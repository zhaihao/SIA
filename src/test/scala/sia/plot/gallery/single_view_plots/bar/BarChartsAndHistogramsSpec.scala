/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.plot.gallery.single_view_plots.bar

import os.RelPath
import sia.plot.gallery.Dataset
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
      .withValues(Seq(
        Map("a" -> "A", "b" -> 28),
        Map("a" -> "B", "b" -> 55),
        Map("a" -> "C", "b" -> 43),
        Map("a" -> "D", "b" -> 91),
        Map("a" -> "E", "b" -> 81),
        Map("a" -> "F", "b" -> 53),
        Map("a" -> "G", "b" -> 19),
        Map("a" -> "H", "b" -> 87),
        Map("a" -> "I", "b" -> 52)
      ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/1.json"))
      .html
      .browse()
  }

  "Aggregate Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/2.json"))
      .html
      .browse()
  }

  "Aggregate Bar Chart (Sorted)" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/3.json"))
      .html
      .browse()
  }

  "Histogram" in {
    plot.vega
      .withUrl(Dataset.Movies)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/4.json"))
      .html
      .browse()
  }

  "Histogram (from Binned Data)" in {
    plot.vega
      .withValues(Seq(
        Map("bin_start" -> 8, "bin_end"  -> 10, "count" -> 7),
        Map("bin_start" -> 10, "bin_end" -> 12, "count" -> 29),
        Map("bin_start" -> 12, "bin_end" -> 14, "count" -> 71),
        Map("bin_start" -> 14, "bin_end" -> 16, "count" -> 127),
        Map("bin_start" -> 16, "bin_end" -> 18, "count" -> 94),
        Map("bin_start" -> 18, "bin_end" -> 20, "count" -> 54),
        Map("bin_start" -> 20, "bin_end" -> 22, "count" -> 17),
        Map("bin_start" -> 22, "bin_end" -> 24, "count" -> 5)
      ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/5.json"))
      .html
      .browse()
  }

  "Log-scaled Histogram" in {
    plot.vega
      .withValues(
        Seq(
          Map("x" -> 0.01),
          Map("x" -> 0.1),
          Map("x" -> 1),
          Map("x" -> 1),
          Map("x" -> 1),
          Map("x" -> 1),
          Map("x" -> 10),
          Map("x" -> 10),
          Map("x" -> 100),
          Map("x" -> 500),
          Map("x" -> 800)
        ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/6.json"))
      .html
      .browse()
  }

  "Grouped Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/7.json"))
      .html
      .browse()
  }

  "Stacked Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Seattle_Weather)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/8.json"))
      .html
      .browse()
  }

  "Horizontal Stacked Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Barley)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/9.json"))
      .html
      .browse()
  }

  "Normalized (Percentage) Stacked Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/10.json"))
      .html
      .browse()
  }

  "Gantt Chart (Ranged Bar Marks)" in {
    plot.vega
      .withValues(
        Seq(
          Map("task" -> "A", "start" -> 1, "end" -> 3),
          Map("task" -> "B", "start" -> 3, "end" -> 8),
          Map("task" -> "C", "start" -> 8, "end" -> 10)
        ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/11.json"))
      .html
      .browse()
  }

  "A Bar Chart Encoding Color Names in the Data" in {
    plot.vega
      .withValues(
        Seq(
          Map("color" -> "red", "b"   -> 28),
          Map("color" -> "green", "b" -> 55),
          Map("color" -> "blue", "b"  -> 43)
        ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/12.json"))
      .html
      .browse()
  }

  "Layered Bar Chart" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/13.json"))
      .html
      .browse()
  }

  "Diverging Stacked Bar Chart (Population Pyramid)" in {
    plot.vega
      .withUrl(Dataset.Population)
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/14.json"))
      .html
      .browse()
  }

  "Diverging Stacked Bar Chart (with Neutral Parts)" in {
    plot.vega
      .withValues(Seq(
        Map("question"   -> "Question 1",
            "type"       -> "Strongly disagree",
            "value"      -> 24,
            "percentage" -> 0.7),
        Map("question"   -> "Question 1", "type" -> "Disagree", "value" -> 294, "percentage" -> 9.1),
        Map("question"   -> "Question 1",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 594,
            "percentage" -> 18.5),
        Map("question"   -> "Question 1", "type" -> "Agree", "value" -> 1927, "percentage" -> 59.9),
        Map("question"   -> "Question 1",
            "type"       -> "Strongly agree",
            "value"      -> 376,
            "percentage" -> 11.7),
        Map("question"   -> "Question 2",
            "type"       -> "Strongly disagree",
            "value"      -> 2,
            "percentage" -> 18.2),
        Map("question"   -> "Question 2", "type" -> "Disagree", "value" -> 2, "percentage" -> 18.2),
        Map("question"   -> "Question 2",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 2", "type" -> "Agree", "value" -> 7, "percentage" -> 63.6),
        Map("question"   -> "Question 2",
            "type"       -> "Strongly agree",
            "value"      -> 11,
            "percentage" -> 0),
        Map("question"   -> "Question 3",
            "type"       -> "Strongly disagree",
            "value"      -> 2,
            "percentage" -> 20),
        Map("question"   -> "Question 3", "type" -> "Disagree", "value" -> 0, "percentage" -> 0),
        Map("question"   -> "Question 3",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 2,
            "percentage" -> 20),
        Map("question"   -> "Question 3", "type" -> "Agree", "value" -> 4, "percentage" -> 40),
        Map("question"   -> "Question 3",
            "type"       -> "Strongly agree",
            "value"      -> 2,
            "percentage" -> 20),
        Map("question"   -> "Question 4",
            "type"       -> "Strongly disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 4", "type" -> "Disagree", "value" -> 2, "percentage" -> 12.5),
        Map("question"   -> "Question 4",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 1,
            "percentage" -> 6.3),
        Map("question"   -> "Question 4", "type" -> "Agree", "value" -> 7, "percentage" -> 43.8),
        Map("question"   -> "Question 4",
            "type"       -> "Strongly agree",
            "value"      -> 6,
            "percentage" -> 37.5),
        Map("question"   -> "Question 5",
            "type"       -> "Strongly disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 5", "type" -> "Disagree", "value" -> 1, "percentage" -> 4.2),
        Map("question"   -> "Question 5",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 3,
            "percentage" -> 12.5),
        Map("question"   -> "Question 5", "type" -> "Agree", "value" -> 16, "percentage" -> 66.7),
        Map("question"   -> "Question 5",
            "type"       -> "Strongly agree",
            "value"      -> 4,
            "percentage" -> 16.7),
        Map("question"   -> "Question 6",
            "type"       -> "Strongly disagree",
            "value"      -> 1,
            "percentage" -> 6.3),
        Map("question"   -> "Question 6", "type" -> "Disagree", "value" -> 1, "percentage" -> 6.3),
        Map("question"   -> "Question 6",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 2,
            "percentage" -> 12.5),
        Map("question"   -> "Question 6", "type" -> "Agree", "value" -> 9, "percentage" -> 56.3),
        Map("question"   -> "Question 6",
            "type"       -> "Strongly agree",
            "value"      -> 3,
            "percentage" -> 18.8),
        Map("question"   -> "Question 7",
            "type"       -> "Strongly disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 7", "type" -> "Disagree", "value" -> 0, "percentage" -> 0),
        Map("question"   -> "Question 7",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 1,
            "percentage" -> 20),
        Map("question"   -> "Question 7", "type" -> "Agree", "value" -> 4, "percentage" -> 80),
        Map("question"   -> "Question 7",
            "type"       -> "Strongly agree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 8",
            "type"       -> "Strongly disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 8", "type" -> "Disagree", "value" -> 0, "percentage" -> 0),
        Map("question"   -> "Question 8",
            "type"       -> "Neither agree nor disagree",
            "value"      -> 0,
            "percentage" -> 0),
        Map("question"   -> "Question 8", "type" -> "Agree", "value" -> 0, "percentage" -> 0),
        Map("question"   -> "Question 8",
            "type"       -> "Strongly agree",
            "value"      -> 2,
            "percentage" -> 100)
      ))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/15.json"))
      .html
      .browse()
  }
  "Simple Bar Chart with Labels" in {
    plot.vega.withValues(Seq(
    Map("a"-> "A", "b"-> 28),
    Map("a"-> "B", "b"-> 55),
    Map("a"-> "C", "b"-> 43)))
      .viz(os.resource / RelPath("sia/plot/gallery/single_view_plots/bar/16.json"))
      .html
      .browse()
  }
}
