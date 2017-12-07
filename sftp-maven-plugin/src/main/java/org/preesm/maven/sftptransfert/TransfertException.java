package org.preesm.maven.sftptransfert;

public class TransfertException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = -1231323608285249007L;

  public TransfertException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public TransfertException(final String message) {
    super(message, null);
  }

}
