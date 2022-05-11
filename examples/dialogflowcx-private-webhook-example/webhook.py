# Copyright 2022, Google LLC
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
'''Dialogflow CX webhook endpoint example '''

from datetime import datetime
from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse

from modules.request import WebhookRequest
from modules.response import WebhookResponse

# Initiate FastAPI app
app = FastAPI()

# Define Root entrypoint as `Hello World``
@app.get("/")
def read_root():
    '''Root endpoint entrypoint for test purpose'''
    return {"msg": "Hello World"}

# Define Dialogflow CX entrypoint
@app.post("/webhook/")
async def webhook(webhook_request: WebhookRequest):
    '''Dialogflow CX webhook endpoint'''

    # Uncomment if you need to use Dialofglo parameters
    # parameters = webhook_request.sessionInfo.parameters
    tag = webhook_request.fulfillmentInfo.tag

    # Define Response
    response = WebhookResponse(detectIntentResponseId="1234")
    response.add_param("now", datetime.now(tz=None))

    if tag == "Default Welcome Intent":
        response.add_text_response('Hi from a Python Webhook!')
    elif tag == "echo":
        message = webhook_request.text
        response.add_text_response('You said: ' + message)
    else :
        response.add_text_response('There are no fulfillment responses defined for tag: ' + tag)
    json_response = jsonable_encoder(response)
    return JSONResponse(content=json_response)
