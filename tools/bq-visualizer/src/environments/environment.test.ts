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
export const environment = {
  production: false,
  name: 'test',
  bqUrl: 'https://www.googleapis.com/bigquery/v2/projects',

  authConfig: {
    // Url of the Identity Provider
    issuer: 'https://accounts.google.com',

    // URL of the SPA to redirect the user to after login
    redirectUri: window.location.origin + '/jobs',

    // The SPA's id. The SPA is registerd with this id at the auth-server
    clientId:
        '697664373295-i3fqtavcagei5qklsighjb8qv2jkm7f3.apps.googleusercontent.com',

    // set the scope for the permissions the client should request
    // The first three are defined by OIDC. The 4th is a usecase-specific one
    scope: 'profile email https://www.googleapis.com/auth/bigquery.readonly ',
    strictDiscoveryDocumentValidation: false,
    showDebugInformation: true
  },
};
