package pedro.goncalves
package api.models

case class FileStorage(
                      bucket:String,
                      repository:String,
                      versioned:Boolean,
                      version:Float
                      )
