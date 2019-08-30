/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.plot.gallery.single_view_plots.line

import os.RelPath
import plot.spec.Themes
import sia.plot.gallery.Dataset
import test.BaseSpec

/**
  * LineChartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019/8/30 10:20 上午
  */
class LineChartSpec extends BaseSpec {

  "Line Chart" in {
    plot.vega
      .theme(Themes.Vox)
      .withUrl(Dataset.Stocks)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/line/1.json"))
      .html
      .browse()
  }

  "Line Chart with Point Markers" in {
    plot.vega
      .theme(Themes.Vox)
      .withUrl(Dataset.Stocks)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/line/2.json"))
      .html
      .browse()
  }

  "Line Chart with Stroked Point Markers" in {
    plot.vega
      .theme(Themes.Vox)
      .withUrl(Dataset.Stocks)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/line/3.json"))
      .html
      .browse()
  }

  "Multi Series Line Chart" in {
    plot.vega
      .theme(Themes.Vox)
      .withUrl(Dataset.Stocks)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/line/4.json"))
      .html
      .browse()
  }

  "Slope Graph" in {
    plot.vega
      .theme(Themes.Vox)
      .withUrl(Dataset.Barley)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/line/5.json"))
      .html
      .browse()
  }
}
