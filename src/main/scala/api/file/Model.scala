package pedro.goncalves
package api.file

case class RepositoryMetadata(
                bucket:String,
                repository:String,
                versioned:Boolean,
                version:Float
                  )
