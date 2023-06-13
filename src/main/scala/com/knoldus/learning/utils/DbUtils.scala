package com.knoldus.learning.utils

import com.knoldus.learning.Connection
import com.knoldus.learning.utils.Utils.await
import slick.dbio.DBIO

object DbUtils {
  def execute[T](action: DBIO[T]): T = {
    await(Connection.db.run(action))
  }
}
