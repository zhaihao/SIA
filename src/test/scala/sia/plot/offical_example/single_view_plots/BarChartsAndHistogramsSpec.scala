/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.plot.offical_example.single_view_plots

import test.BaseSpec

/**
  * BarChartsAndHistogramsSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-23 17:08
  */
class BarChartsAndHistogramsSpec extends BaseSpec {

  "Simple Bar Chart" in {
    plot.vega
      .json(
        //language=JSON
        """
          |{
          |  "$schema": "https://vega.github.io/schema/vega-lite/v3.json",
          |  "description": "A simple bar chart with embedded data.",
          |  "data": {
          |    "values":  [
          |      {"a": "A", "b": 28}, {"a": "B", "b": 55}, {"a": "C", "b": 43},
          |      {"a": "D", "b": 91}, {"a": "E", "b": 81}, {"a": "F", "b": 53},
          |      {"a": "G", "b": 19}, {"a": "H", "b": 87}, {"a": "I", "b": 52}
          |    ]
          |  },
          |  "mark": {
          |    "type": "bar",
          |    "tooltip": true
          |  },
          |  "encoding": {
          |    "x": {
          |      "field": "a",
          |      "type": "ordinal"
          |    },
          |    "y": {
          |      "field": "b",
          |      "type": "quantitative"
          |    }
          |  }
          |}
          |""".stripMargin)
      .html
      .browse()
  }

}
