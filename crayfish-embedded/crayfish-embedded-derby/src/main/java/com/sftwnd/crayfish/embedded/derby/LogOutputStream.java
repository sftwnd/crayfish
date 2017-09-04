package com.sftwnd.crayfish.embedded.derby;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This class logs all bytes written to it as output stream with a specified logging level.
 *
 * @author <a href="mailto:cspannagel@web.de">Christian Spannagel</a>
 * @version 1.0
 */
public class LogOutputStream extends OutputStream {
    /** The logger where to log the written bytes. */
    private Logger logger;

    /** The level. */
    private Level level;

    /** The internal memory for the written bytes. */
    private ByteBuffer mem;

    /**
     * Creates a new log output stream which logs bytes to the specified logger with the specified
     * level.
     *
     * @param logger the logger where to log the written bytes
     * @param level the level
     */
    public LogOutputStream (Logger logger, Level level) {
        setLogger (logger);
        setLevel (level);
        mem = null;
    }

    /**
     * Sets the logger where to log the bytes.
     *
     * @param logger the logger
     */
    public void setLogger (Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the logger.
     *
     * @return DOCUMENT ME!
     */
    public Logger getLogger () {
        return logger;
    }

    /**
     * Sets the logging level.
     *
     * @param level DOCUMENT ME!
     */
    public void setLevel (Level level) {
        this.level = level;
    }

    /**
     * Returns the logging level.
     *
     * @return DOCUMENT ME!
     */
    public Level getLevel () {
        return level;
    }

    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b DOCUMENT ME!
     */
    public void write (int b) {
        if (b == '\n') {
            flush();
        } else {
            if (mem == null) {
                mem = ByteBuffer.allocate(15);
            } else if (mem.position() == mem.limit()) {
                mem = ByteBuffer.allocate(mem.limit() * 2).put(mem.array(), 0, mem.position());
            }
            mem.put((byte)(b & 0xFF));
        }
    }

    /**
     * Flushes the output stream.
     */
    public void flush () {
        if (mem != null) {
            mem.flip();
            try {
                switch (level) {
                    case ERROR:
                        logger.error(new String(mem.array(), 0, mem.limit()));
                        break;
                    case WARN:
                        logger.warn(new String(mem.array(), 0, mem.limit()));
                        break;
                    case DEBUG:
                        logger.debug(new String(mem.array(), 0, mem.limit()));
                        break;
                    case TRACE:
                        logger.trace(new String(mem.array(), 0, mem.limit()));
                        break;
                    default:
                        logger.info(new String(mem.array(), 0, mem.limit()));
                }
            } finally {
                mem = null;
            }
        }
    }

}