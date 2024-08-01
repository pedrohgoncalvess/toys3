package pedro.goncalves
package api.bucket.routers


import scala.util.{Failure, Success}

import org.json4s.{JArray, JObject, JString}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes

import api.bucket.exceptions.BucketExists
import api.auth.Service.endpointAuthenticator
import s3.organizer.implementations.Bucket
import api.bucket.models.{CreateBucket, PostJsonSupport}


class Post extends Directives with PostJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic 
      post {
        entity(as[CreateBucket]) { bucket =>
          val bucketOperations = Bucket(bucket.name)

          if (bucketOperations.check)
            throw BucketExists(bucket.name)

          val jDescription = if bucket.description.orNull != null then JObject("description" -> JString(bucket.description.get)) else null
          val jTags = if bucket.tags.orNull != null then JObject("tags" -> JArray(bucket.tags.get.map(i => JString(i)).toList)) else null

          val externalMetadata: Option[JObject] =
            if jDescription != null || jTags != null
              then Some(jDescription.merge(jTags))
            else
              Some(null)

          onComplete(bucketOperations.create(externalMetadata)) {
            case Success(_) => complete(StatusCodes.OK)
            case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }
  }


