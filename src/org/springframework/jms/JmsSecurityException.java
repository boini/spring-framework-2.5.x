/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms;


/**
 * Spring Framework runtime exception mirroring the JMS
 * {@link javax.jms.JMSSecurityException}.  This exception is thrown when
 * encountering its JMS counterpart so that Spring Runtime exception
 * behavior is maintained.
 *
 * @see javax.jms.JMSSecurityException
 * 
 * @author Les Hazlewood
 *
 */
public class JmsSecurityException extends JmsException {

    /**
     * Simple constructor allowing a message that explains the nature of the
     * exception.
     *
     * @param s The message that explains the purpose of the exception.
     */
    public JmsSecurityException( String s ) {
        super( s );
    }


    /**
     * Constructor allowing initialization with a message and a cause.
     *
     * @param s  he message that explains the purpose of the exception.
     * @param ex The cause of the exception
     */
    public JmsSecurityException( String s, Throwable ex ) {
        super( s, ex );
    }


    /**
     * Constructor allowing initialization with a cause.  This constructor is
     * generally provided as a means for wrapping the actual JMS checked
     * exception (e.g {@link javax.jms.JMSSecurityException}) that triggered
     * this exception.
     *
     * @param cause the cause of this exception
     */
    public JmsSecurityException( Throwable cause ) {
        super( cause );
    }

}


/*
 * Modification history:
 *
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2004/07/08 13:09:32  markpollack
 * initial import from sandbox
 *
 * Revision 1.1  2004/07/01 18:12:38  markpollack
 * rework QOS support, add Les's ExceptionConversion, add defaultDestination, add ProducerCallback support.
 *
 */
