package models

import scala.util.Random

/**
 * Created by Ömer Faruk Gül on 25/01/15.
 */
case class User(username: String) {
	//def apply(usernmae:String) = new User(username)

	def generateCode:String = {
		val x = Random.alphanumeric
		username+"-"+(x take 5).mkString
	}
}
