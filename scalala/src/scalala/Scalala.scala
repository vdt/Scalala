/*
 * Distributed as part of Scalala, a linear algebra library.
 * 
 * Copyright (C) 2008- Daniel Ramage
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA 
 */
package scalala;

import ScalalaValues._
import ScalalaOps._
import ScalalaMTJ._

/**
 * A matlab-like environment and syntax for scala.
 */
object Scalala {
  
  //
  // implicit context values
  //
  
  implicit var _scalala_pool = new EvaluationPool();
  
  /** Implicit graphics context */
  implicit var _scalab_figures = new Plotting.Figures();
  implicit def _scalab_figure = _scalab_figures.figure;
  implicit def _scalab_xyplot = _scalab_figures.figure.plot;
    
  implicit def iFigure(figures : Plotting.Figures) = figures.figure;
  implicit def iXYPlot(figures : Plotting.Figures) = figures.figure.plot;
  implicit def iXYPlot(figure  : Plotting.Figure)  = figure.plot;
  
  
  ////////////////////////////////////////////////////////////////////////}
  //
  // Feature-complete Matlab commands
  //
  // Primary difference is that some commands that normally return an
  // n x n square matrix now return a column vector of size n. e.g.
  // ones(n) here is a vector that would be ones(n,1) in matlab.  The
  // main reason for the distinction is that Vector and Matrix are
  // different types in MTJ, so ones(n,1) returns a Matrix of size n by 1
  // which shouldn't need its own conversion back to a vector.
  //
  ////////////////////////////////////////////////////////////////////////
  
  val NaN = java.lang.Double.NaN
  def isnan(a : Double) : Boolean = java.lang.Double.isNaN(a)
    
  /** 100 evenly spaced points between a and b */
  def linspace(a : Double, b : Double) : Vector = linspace(a,b,100)
  
  /** n evenly spaced points between a and b */
  def linspace(a : Double, b : Double, n : Int) : Vector = {
    val v = DenseVector(n)
    val delta = (b - a) / (n - 1.0)
    for (i <- 0 until n) { v.set(i, a + i*delta) }
    return v
  }
  
  /** A vector of ones of the given size */
  def ones(n : Int) : Vector = {
    val v = DenseVector(n)
    for (i <- 0 until n) v.set(i,1.0);
    return v
  }
  
  /** A matrix of size m by n with 1 everywhere */
  def ones(rows : Int, cols : Int) : Matrix = {
    val m = DenseMatrix(rows,cols)
    for (i <- 0 until rows; j <- 0 until cols) m.set(i,j,1.0);
    return m
  }
  
  /** A vector of zeros of the given size */
  def zeros(n : Int) : Vector = DenseVector(n);
  
  /** A matrix of size m by n with 0 everywhere */
  def zeros(rows : Int, cols : Int) : Matrix = DenseMatrix(rows,cols);
  
  /**
   * Converts a Vector into a DenseVector if necessary, returning a casted
   * reference to the input if possible.
   */
  /*
  def full(vector : Vector) : DenseVector = {
    if (vector.isInstanceOf[DenseVector]) {
      vector.asInstanceOf[DenseVector]
    } else {
      new DenseVector(vector);
    }
  }
  */
  
  /**
   * Converts a Vector into a DenseVector if necessary, returning a casted
   * reference to the input if possible.
   */
  /*
  def full(matrix : Matrix) : DenseMatrix = {
    if (matrix.isInstanceOf[DenseMatrix]) {
      matrix.asInstanceOf[DenseMatrix]
    } else {
      new DenseMatrix(matrix);
    }
  }
  */
  
  /** Sums the elements of a vector */
  def sum(v : Vector) : Double = v.map(_.get).reduceLeft(_ + _)
  
  /** Sums the columns of the given matrix, returning a row vector */
  def sum(m : Matrix) : Matrix = sum(m, 1);
  
  /** Sums along the given dimension of the matrix (1 for rows, 2 for cols) */
  def sum(m : Matrix, dim : Int) : Matrix = {
    dim match {
      case 1 => {
        val sum = DenseMatrix(1, m.cols);
        for (entry <- m) {
          sum(0, entry.col) += entry.get;
        }
        sum;
      }
      case 2 => {
        val sum = DenseMatrix(m.rows, 1);
        for (entry <- m) {
          sum(entry.row, 0) += entry.get;
        }
        sum;
      }
      case _ => {
        throw new IndexOutOfBoundsException
      }
    }
  }
  
  /** The maximum element of a vector */
  def max(v : Vector) : Double = v.map(_.get).reduceLeft(Math.max)
  
  /** The minimum element of a vector */
  def min(v : Vector) : Double = v.map(_.get).reduceLeft(Math.min)
  
  ////////////////////////////////////////////////////////////////////////
  //
  // Partially Matlab-compatible implementations
  //
  ////////////////////////////////////////////////////////////////////////
  
  //
  // File IO
  //
  
  def dlmread(file : String) : Matrix = {
    scala.io.Source.fromFile(file).getLines.map {
      (line:String) => line.trim.split("\\s+").map(f => java.lang.Double.parseDouble(f)).toArray
    }.toList
  }
  
  //
  // Random number generation
  //
  
  import java.util.Random
  implicit var _scalab_random = new java.util.Random
  
  /** Returns a psuedo-random number from the interval 0 to 1 */
  def rand()(implicit rand : Random) = rand.nextDouble;
  
  /** Returns vector of size n, each element from 0 to 1 */
  def rand(n : Int)(implicit rand : Random) : Vector = {
    val v = DenseVector(n);
    for (i <- 0 until n) {
      v(i) = rand.nextDouble;
    }
    return v;
  }
  
  /** Returns a random matrix of the given size, each element drawn from 0 to 1 */
  def rand(rows : Int, cols : Int)(implicit rand : Random) : Matrix = {
    val m = DenseMatrix(rows,cols);
    for (i <- 0 until rows; j <- 0 until cols) {
      m.set(i,j,rand.nextDouble);
    }
    return m;
  }
  
  /** Returns a pseudo-random gaussian variable */
  def randn()(implicit rand : Random) = rand.nextGaussian
  
  /** Returns a vector of size n, each element from a gaussian*/
  def randn(n : Int)(implicit rand : Random) : Vector = {
    val v = DenseVector(n);
    for (i <- 0 until n) {
      v(i) = rand.nextGaussian;
    }
    return v;
  }
  
  /** Returns a random matrix of the given size, each element drawn from a gaussian */
  def randn(rows : Int, cols : Int)(implicit rand : Random) : Matrix = {
    val m = DenseMatrix(rows,cols);
    for (i <- 0 until rows; j <- 0 until cols) {
      m.set(i,j,rand.nextGaussian);
    }
    return m;
  }
  
  /** Returns a square diagonal matrix of the given size */
  def diag(n : Int) : Matrix = {
    return diag(ones(n));
  }
  
  /** Returns a diagonal matrix with the given vector on the diagonal */
  def diag(v : Vector) : Matrix = {
    val nz : Array[Array[Int]] = (0 until v.size).map(i => Array(i)).toArray
    val m = new no.uib.cipr.matrix.sparse.CompColMatrix(v.size, v.size, nz)
    for (i <- 0 until v.size) {
      m.set(i,i,v.get(i))
    }
    return m
  }
  
  /** Turns the given matrices into a block diagonal matrix */
//  def blkdiag(blocks : Seq[Matrix]) : Matrix = {
//    def zeros : Array[Matrix] =
//      blocks.map(m => ScalarMatrix(0.0, m.rows, m.cols)).toArray
//      
//    def row(pos : Int) : Array[Matrix] = {
//      val row = zeros
//      row(pos) = blocks(pos)
//      row
//  }
//    
//    new BlockMatrix((0 until blocks.length).map{row}.toArray)
//  }
  
  //
  // Plotting
  //

  /** Selects the given figure */
  def figure(select:Int)(implicit figures : Plotting.Figures) : Plotting.Figure = {
    figures.figure = select-1
    figures.figure.refresh
    return figures.figure
  }
  
  /** Selects the given subplot */
  def subplot(rows:Int,cols:Int,select:Int)(implicit figure : Plotting.Figure) : Plotting.XYPlot = {
    figure.rows = rows
    figure.cols = cols
    figure.plot = select-1
    figure.refresh
    return figure.plot
  }
  
  /**
   * Sets tooltips for the last series plotted (assumes x,y series).
   * NB: This is not standard Matlab.
   */
  def tooltips(tips : Seq[String])(implicit xyplot : Plotting.XYPlot) {
    xyplot.plot.getRenderer(xyplot.series).setBaseToolTipGenerator(Plotting.Tooltips(tips))
  }
  
  /** Plots a histogram of the given data into 10 equally spaced bins */
  def hist(data : Vector)(implicit xyplot : Plotting.XYPlot) : Unit = {
    hist(data, 10)(xyplot)
  }
  
  
  /** Plots a histogram of the given data into the given number of bins */
  def hist(data : Vector, nbins : Int)(implicit xyplot : Plotting.XYPlot) : Unit = {
    hist(data, linspace(min(data),max(data),nbins))(xyplot)
  }
  
  /**
   * Plots a histogram of the given data into bins with centers at the given
   * positions.
   */
  def hist(data : Vector, bins : Vector)(implicit xyplot : Plotting.XYPlot) : Unit = {
    def bucket(point : Double, lower : Int, upper : Int) : Int = {
      val mid = (lower + upper) / 2 
      if (lower == upper) {
        return upper
      } else if (point < bins.get(mid)) {
        return bucket(point, lower, mid)
      } else {
        return bucket(point, mid+1, upper)
      }
    }
    
    val counts = DenseVector(bins.size)
    for (point <- data) {
      val bin = bucket(point.get, 0, bins.size-1)
      counts.set(bin, counts.get(bin) + 1)
    }
    
    // smallest gap between bins
    val width = { bins.map(_.get).elements zip (bins.map(_.get).elements drop 1) map
      (pair => Math.abs(pair._2-pair._1)) reduceLeft Math.min }
    
    val dataset = new org.jfree.data.xy.XYBarDataset(
      Plotting.Dataset(bins, counts), width)
    val series = xyplot.nextSeries
    xyplot.plot.setDataset(series,dataset)
    xyplot.plot.setRenderer(series,new org.jfree.chart.renderer.xy.XYBarRenderer)
    xyplot.refresh
  }
  
  /** Plots the given y versus the given x with line drawn */
  def plot(x : Vector, y : Vector)(implicit xyplot : Plotting.XYPlot) : Unit = {
    plot(x,y,'-')(xyplot)
  }
  
  /** Plots the given y versus the given x with the given style */
  def plot(x : Vector, y : Vector, style : Char)(implicit xyplot : Plotting.XYPlot) : Unit = {
    lazy val shapeDot = new java.awt.geom.Ellipse2D.Double(0,0,2,2);
    lazy val shapePlus = {
      val shape = new java.awt.geom.Path2D.Double();
      shape.moveTo(-3,0)
      shape.lineTo(3,0)
      shape.moveTo(0,-3)
      shape.lineTo(0,3)
      shape
    }
  
    
    // initialize dataset and series
    val dataset = Plotting.Dataset(x,y)
    val series = xyplot.nextSeries
    
    xyplot.plot.setDataset(series, dataset)
    
    // set the renderer
    import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
    val renderer = new XYLineAndShapeRenderer()
    
    style match {
    case '-' => {
        renderer.setLinesVisible(true)
        renderer.setShapesVisible(false)
      }
    case '.' => {
        renderer.setLinesVisible(false)
        renderer.setShapesVisible(true)
        renderer.setShape(shapeDot)
      }
    case '+' => {
        renderer.setLinesVisible(false)
        renderer.setShapesVisible(true)
        renderer.setShape(shapePlus)
      }
    case _ => { }
    }
    
    xyplot.plot.setRenderer(series, renderer)
    xyplot.refresh
  }
  
  /**
   * Displays a scatter plot of x versus y, each point drawn at the given
   * size and mapped with the given color.
   */
  def scatter(x : Vector, y : Vector, s : Vector, c : Vector)(implicit xyplot : Plotting.XYPlot) {
    assert(x.size == y.size)
    assert(y.size == s.size, y.size + " != " + s.size)
    assert(s.size == c.size, s.size + " != " + c.size)
    
    val dataset = Plotting.Dataset(x,y,s,c)
    val series = xyplot.nextSeries
    xyplot.plot.setDataset(series, dataset)
    
    val gradient = Gradients.GRADIENT_BLUE_TO_RED
    
    val paintscale = new org.jfree.chart.renderer.PaintScale {
      override def getLowerBound = 0.0
      override def getUpperBound = 1.0
      override def getPaint(value : Double) = {
        val index = gradient.length * (value - getLowerBound) / (getUpperBound - getLowerBound)
        gradient(Math.min(gradient.length-1, Math.max(0, index.toInt)))
      }
    }
    
    // set the renderer
    import java.awt.Graphics2D
    import java.awt.geom.Rectangle2D
    import org.jfree.data.xy.XYDataset
    import org.jfree.ui.RectangleEdge
    import org.jfree.chart.axis.ValueAxis
    import org.jfree.chart.renderer.xy.AbstractXYItemRenderer
    import org.jfree.chart.renderer.xy.XYBubbleRenderer
    import org.jfree.chart.renderer.xy.XYItemRendererState
    val renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_DOMAIN_AXIS) {
      val stroke = new java.awt.BasicStroke(0f)
      override def getItemPaint(series : Int, item : Int) : java.awt.Paint = {
        paintscale.getPaint(c.get(item))
      }
      override def getItemStroke(series : Int, item : Int) = stroke
    }
    
    xyplot.plot.setRenderer(series, renderer)
    xyplot.refresh
  }
  
  def scatter(x : Vector, y : Vector, s : Double, c : Vector)(implicit xyplot : Plotting.XYPlot) {
    scatter(x,y,ones(x.size)*s,c)(xyplot)
  }
  
  def scatter(x : Vector, y : Vector, s : Vector, c : Double)(implicit xyplot : Plotting.XYPlot) {
    scatter(x,y,s,ones(x.size)*c)(xyplot)
  }
  
  /** Plots the given matrix as an image. */
  def imagesc(c : Matrix)(implicit xyplot : Plotting.XYPlot) {
    val inverted = new Matrix {
      override def rows = c.rows;
      override def cols = c.cols;
      override def get(row : Int, col : Int) = c.get(rows-row-1,col);
      override def set(row : Int, col : Int, x : Double) = c.set(rows-row-1,col,x);
    }
    
    val dataset = Plotting.Dataset(inverted)
    val series = xyplot.nextSeries
    
    xyplot.plot.setDataset(series, dataset)
    
    import org.jfree.chart.renderer.xy.XYBlockRenderer
    val renderer = new XYBlockRenderer()
    renderer.setBlockAnchor(org.jfree.ui.RectangleAnchor.TOP_LEFT)
    
    val gradient = Gradients.GRADIENT_BLUE_TO_RED
    
    val paintscale = new org.jfree.chart.renderer.PaintScale {
      override def getLowerBound = 0.0
      override def getUpperBound = 1.0
      override def getPaint(value : Double) = {
        val index = gradient.length * (value - getLowerBound) / (getUpperBound - getLowerBound)
        gradient(Math.min(gradient.length-1, Math.max(0, index.toInt)))
      }
    }
    
    renderer.setPaintScale(paintscale)
    xyplot.plot.getRangeAxis.setInverted(true)
    xyplot.plot.getRangeAxis.setLowerBound(0)
    xyplot.plot.getRangeAxis.setUpperBound(c.rows)
    xyplot.plot.getDomainAxis.setLowerBound(0)
    xyplot.plot.getDomainAxis.setUpperBound(c.cols)
    xyplot.plot.setRenderer(series, renderer)
    xyplot.refresh
  }
  
  /** For re-plotting to same figure */
  def hold(state : Boolean)(implicit figures : Plotting.Figures) : Unit = {
    val xyplot = figures.figure.plot
    xyplot.hold = state
  }
  
  ////////////////////////////////////////////////////////////////////////}
  //
  // Statistical methods
  //
  ////////////////////////////////////////////////////////////////////////
  
  /**
   * Computes the Pearson correlation coefficient between the two vectors.
   * Code adapted excerpted from Wikipedia:
   *   http://en.wikipedia.org/wiki/Pearson%27s_correlation_coefficient
   */
  def corr(x : Vector, y : Vector) : Double = {
    val N = x.size
    var sum_sq_x = 0.0
    var sum_sq_y = 0.0
    var sum_coproduct = 0.0
    var mean_x = x.get(0)
    var mean_y = y.get(0)
    for (i <- 1 until N) {
      val sweep = (i - 1.0) / i
      val delta_x = x.get(i) - mean_x
      val delta_y = y.get(i) - mean_y
      sum_sq_x += delta_x * delta_x * sweep
      sum_sq_y += delta_y * delta_y * sweep
      sum_coproduct += delta_x * delta_y * sweep
      mean_x += delta_x / i
      mean_y += delta_y / i
    }
    val pop_sd_x = Math.sqrt( sum_sq_x / N )
    val pop_sd_y = Math.sqrt( sum_sq_y / N )
    val cov_x_y = sum_coproduct / N
    return cov_x_y / (pop_sd_x * pop_sd_y)
  }
}