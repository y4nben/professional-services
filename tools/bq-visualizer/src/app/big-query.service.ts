/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {concat, defer, EMPTY, from, Observable, Observer, of, Subscription} from 'rxjs';
import {catchError, filter, map} from 'rxjs/operators';

import {environment} from '../environments/environment';

import {BqJob} from './bq_job';
import {GoogleAuthService} from './google-auth.service';
import {LogService} from './log.service';
import {BqListJobResponse, BqProject, BqProjectListResponse, Job} from './rest_interfaces';

export type GetJobsReturn = [Observable<BqJob>, () => any];

/** All services that talk to the BigQuery API. */
@Injectable({providedIn: 'root'})
export class BigQueryService {
  projectList: BqProject[] = [];
  projectFilter: string;
  lastProjectId: string;

  constructor(
      private http: HttpClient,  // private oauthService: OAuthService,
      private googleAuthService: GoogleAuthService,
      private logSvc: LogService) {}

  /** Get the detail of a job. */
  getQueryPlan(projectId: string, jobId: string, location: string):
      Observable<Job> {
    // Extract job id if the job id is a collection of
    // [project]:[location].jobId
    const realid = jobId.split('.').slice(-1)[0];
    this.logSvc.debug(`getQueryPlan: fetched query plan for jobid=${jobId}`);

    const token = this.googleAuthService.getAccessToken();

    const args = {access_token: token, location: location};
    const url = bqUrl(`/${projectId}/jobs/${realid}`, args);

    this.logSvc.debug(`Requested job detail for: ${realid}`);
    return this.http.get<Job>(url).pipe(
        catchError(this.handleError('getQueryPlan')));
  }

  /** Get all jobs for a project. */
  getJobs(projectId: string, maxJobs: number, allUsers: boolean):
      Observable<BqJob> {
    return Observable.create(async (obs:Observer<BqJob>) => {
      const token = this.googleAuthService.getAccessToken();
      let nextPageToken = '';
      let totalJobs = 0;
      while (true) {
        const url = bqUrl(`/${projectId}/jobs`, {
          access_token: token,
          maxResults: 200,
          allUsers: allUsers,
          projection: 'full',
          pageToken: nextPageToken,
        });

        try {
          await new Promise((resolve, reject) => {
            this.http.get<BqListJobResponse>(url).subscribe(
                res => {
                  if (!res.jobs) {
                    console.error(`No jobs found in bq response`, res);
                    if (allUsers) {
                      alert(
                          `There were no jobs found that you can view. To ` +
                          `list jobs for all users, you ` +
                          `need the Owner permission on the project.`);
                    } else {
                      alert('There were no jobs found that you can view.');
                    }
                    throw new Error('No jobs found');
                  }
                  for (const job of res.jobs.map(el => new BqJob(el))) {
                    if (obs.closed) return;
                    obs.next(job);
                  }
                  nextPageToken = res.nextPageToken;
                  totalJobs += res.jobs.length;
                  if (totalJobs >= maxJobs) {
                    obs.complete();
                    return;
                  }
                },
                err => {
                  console.error(`Error loading jobs: ${err}`);
                  throw new Error(err);
                },
                () => {
                  resolve([]);
                });
          });
        } catch (err) {
          obs.error(err);
        }

        if (!nextPageToken || obs.closed) {
          obs.complete();
          return;
        }
      }
    });
  }

  /** Get all projects. */
  getProjects(): Observable<BqProject> {
    return Observable.create(async (obs:Observer<BqProject>)=> {
      if (this.googleAuthService.isLoggedIn() === false) {
        await this.googleAuthService.login();
        if (this.googleAuthService.isLoggedIn()) {
          this.logSvc.info('successfully Logged in');
        } else {
          this.logSvc.error  ('failed Logged in');
          obs.error('No authentication token available.');
        }
      }
      const token = this.googleAuthService.getAccessToken();

      let nextPageToken = '';
      while (true) {
        const url = bqUrl('', {
          access_token: token,
          maxResults: 1000,
          pageToken: nextPageToken,
        });

        try {
          await new Promise((resolve, reject) => {
            this.http.get<BqProjectListResponse>(url).subscribe(
                res => {
                  if (!res.projects) {
                    throw new Error('No projects found');
                  }
                  for (const project of res.projects) {
                    if (obs.closed) return;
                    obs.next(project);
                  }
                  nextPageToken = res.nextPageToken;
                },
                err => {
                  console.error(`Error loading projects: ${err}`);
                  throw (err);
                },
                () => {
                  resolve([]);
                });
          });
        } catch (err) {
          obs.error(err);
        }

        if (!nextPageToken || obs.closed) {
          obs.complete();
          return;
        }
      }
    });
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   */
  private handleError(operation = 'operation'): any {
    return (error: any): Observable<any> => {
      this.logSvc.error(`${operation} failed: ${error.message}`);
      if (error.error.error.message) {
        this.logSvc.error(
            `${operation} failed(2): ${error.error.error.message}`);
        alert(`${operation} failed: ${error.error.error.message}`);
      }
      return of([]);
    };
  }
}

function bqUrl(path: string, args: any): string {
  let url = environment.bqUrl + path;
  if (args) {
    let opts = [];
    for (const key of Object.keys(args)) {
      if (args[key]) {
        opts.push(
            encodeURIComponent(key) + '=' + encodeURIComponent(args[key]));
      }
    }
    url += '?' + opts.join('&');
  }
  return url;
}

@Injectable({providedIn: 'root'})
export class MockBigQueryService extends BigQueryService {
  getJobs(projectId: string, maxJobs: number): Observable<BqJob> {
    return from<BqJob[]>(require('../assets/test/get_jobs.json').jobs);
  }

  getProjects(): Observable<BqProject> {
    return from<BqProject[]>(require('../assets/test/get_projects.json').projects);
  }
}
