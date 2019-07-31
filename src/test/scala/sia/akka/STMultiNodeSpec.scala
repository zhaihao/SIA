/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.BeforeAndAfterAll
import test.BaseSpecLike

/**
  * STMultiNodeSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-31 16:47
  */
trait STMultiNodeSpec extends MultiNodeSpecCallbacks with BaseSpecLike with BeforeAndAfterAll {
  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
