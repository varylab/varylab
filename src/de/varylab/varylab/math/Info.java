// JTEM - Java Tools for Experimental Mathematics
// Copyright (C) 2001 JEM-Group
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.varylab.varylab.math;

import java.io.OutputStream;

/**
 * The class can be used to save the debug and iteration information in some classes of this package.
 * @author Markus Schmies, Vitali Lieder
 * @version 1.0
 */
public final class Info
    implements java.io.Serializable {


	private static final long serialVersionUID = 1L;
	private boolean debug;
  private String message = "";
  private int currentIter = 0, maxIter = 0;

  /**
   * Constructs Info object for minimizing process.
   * @param debug indicates printing debug output.
   */
  public Info( final boolean debug ) {
    this.debug= debug;
  }

  /**
   * Constructs Info object for minimizing process.
   */
  public Info() {
    this(false);
  }

  /**
   * Sets debug for indicating printing debug ouput.
   * @param debug indicates printing debug output.
   */
  public void setDebug( final boolean debug ){
      this.debug= debug;
  }

  /**
   * Gets the current status of printing debug ouput.
   * @return debug info.
   */
  public boolean getDebug(){
    return debug;
  }


  /**
   * Returns the message.
   * @return message as string.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the reached iteration.
   * @return current iteration number
   */
  public int getCurrentIter() {
    return currentIter;
  }

  /**
   * Returns max number of iteration.
   * @return max. allowed iteration
   */
  public int getMaxIter() {
    return maxIter;
  }

  /**
   * Returns true if max. number of interration was reached otherwise false.
   * @return boolean whether max number of interation was reached.
   */
  public boolean isMaxIterationReached() {
    return currentIter>=maxIter;
  }

  /**
   * Set message BY REFERENCE.
   * @param str message
   */
  void setMessage(final String str) {
    message = str;
  }

  /**
   * Adds message to the end of existing in new line.
   * @param str message
   */
  void addMessage(final String str) {
    message = message +"\n"+  str;
  }

  /**
   * Set iteration that was reached.
   * @param currentIter surrent iteration
   */
  void setCurrentIter(final int currentIter) {
    this.currentIter = currentIter;
  }

  /**
   * Set max number of interation.
   * @param maxIter max interation allowed
   */
  void setMaxIter(final int maxIter) {
    this.maxIter = maxIter;
  }

  /**
   * Prints information storing in this object to output.
   */
  public void printDebug(){
    if( debug )
      System.out.println( toString() );
  }

  /**
   * Prints information storing in this object to out stream.
   * @param out stream where the info will be printed.
   * @throws java.io.IOException #see OutputStream.write( bytes[] ).
   */
  public void printDebug( OutputStream out ) throws java.io.IOException{
    if ( debug )
      out.write( toString().getBytes() );
  }

  /**
   * Returns a string representation of the object.
   * @return a string representation of the object.
   */
  @Override
public String toString() {
    return "Max Iteration in method: " + maxIter
        + ", reached iteration: " + currentIter + "\nMessage: " + message;
  }
}
