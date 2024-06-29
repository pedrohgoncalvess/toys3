package pedro.goncalves
package api.controllers.file

case class Model(
                      bucket:String,
                      repository:String,
                      versioned:Boolean,
                      version:Float
                      )
