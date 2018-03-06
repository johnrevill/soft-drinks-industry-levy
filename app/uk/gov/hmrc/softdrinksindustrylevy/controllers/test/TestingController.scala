/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.softdrinksindustrylevy.controllers.test

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.softdrinksindustrylevy.connectors.{FileUploadConnector, TestConnector}
import uk.gov.hmrc.softdrinksindustrylevy.services.MongoBufferService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestingController @Inject()(testConnector: TestConnector,
                                  buffer: MongoBufferService,
                                  fileUpload: FileUploadConnector)
  extends BaseController {

  def resetStore: Action[AnyContent] = Action.async {
    implicit request =>
      testConnector.sendReset map {
        r => Status(r.status)
      }
  }

  def resetDb: Action[AnyContent] = Action.async {
    implicit request =>
      buffer.drop.flatMap(_ => Future(Status(OK)))
  }

  def getFile(envelopeId: String, fileName: String) = Action.async { implicit request =>
    fileUpload.getFile(envelopeId, fileName) map { file => Ok(file) }
  }

}