package pedro.goncalves
package api.controllers.file

case class FileStorage(
                      bucket:String,
                      repository:String,
                      versioned:Boolean,
                      version:Float
                      )
