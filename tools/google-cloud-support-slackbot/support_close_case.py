#!/usr/bin/env python3

# Copyright 2022 Google LLC

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import slack
import requests
import logging
from datetime import datetime
from get_parent import get_parent
from case_not_found import case_not_found
from googleapiclient.discovery import build_from_document

logger = logging.getLogger(__name__)


def support_close_case(channel_id, case, user_id):
    """
    Add a comment to a Google Cloud support case.

    Parameters
    ----------
    channel_id : str
        unique string used to idenify a Slack channel. Used to send messages to the channel
    case : str
        unique id of the case
    user_id : str
        the Slack user_id of the user who submitted the request. Used to send ephemeral
        messages to the user
    """
    API_KEY = os.environ.get('API_KEY')
    MAX_RETRIES = 3

    # Get our discovery doc and build our service
    r = requests.get('https://cloudsupport.googleapis.com/$discovery/rest'
                     '?key={}&labels=V2_TRUSTED_TESTER&version=v2beta'.format(API_KEY))
    r.raise_for_status()
    support_service = build_from_document(r.json())

    client = slack.WebClient(token=os.environ.get('SLACK_TOKEN'))
    client.chat_postEphemeral(
        channel=channel_id,
        user=user_id,
        text="Your request is processing ...")
    parent = get_parent(case)

    if parent == 'Case not found':
        case_not_found(channel_id, user_id, case)
    else:
        req = support_service.cases().close(name=parent)
        try:
            req.execute(num_retries=MAX_RETRIES)
        except BrokenPipeError as e:
            error_message = str(e) + ' : {}'.format(datetime.now())
            logger.error(error_message)
            client.chat_postEphemeral(
                channel=channel_id,
                user=user_id,
                text="Your case may not have closed. Please try again later.")
        else:
            client.chat_postEphemeral(
                channel=channel_id,
                user=user_id,
                text=f"You closed case {case}")


if __name__ == "__main__":
    channel_id = os.environ.get('TEST_CHANNEL_ID')
    case = 'xxxxxxxx'
    user_id = os.environ.get('TEST_USER_ID')
    support_close_case(channel_id, case, user_id)
    case = os.environ.get('TEST_CASE')
    support_close_case(channel_id, case, user_id)
