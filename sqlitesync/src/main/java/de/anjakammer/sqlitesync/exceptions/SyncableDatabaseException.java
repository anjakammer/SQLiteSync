package de.anjakammer.sqlitesync.exceptions;


    public class SyncableDatabaseException extends Exception
    {
        String message = null;

        public SyncableDatabaseException() {
            super();
        }

        public SyncableDatabaseException(String message) {
            super(message);
            this.message = message;
        }

        public SyncableDatabaseException(Throwable cause) {
            super(cause);
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public String getMessage() {
            return message;
        }

    }

