package com.github.esgott.mcpu

import io.circe.generic.extras.Configuration

package object api {

  implicit val circeConfiguration: Configuration =
    Configuration.default
      .withDiscriminator("type")

}
