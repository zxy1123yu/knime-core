/*
 * ------------------------------------------------------------------ *
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   Apr 13, 2007 (mb): created
 */
package org.knime.core.node.workflow;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;

public abstract class NodeExecutionJob implements Callable<Void> {
    
    private final NodeLogger m_logger = NodeLogger.getLogger(getClass());

    private final NodeContainer m_nc;
    private final PortObject[] m_data;

    public NodeExecutionJob(final NodeContainer snc, final PortObject[] data) {
        if (snc == null || data == null) {
            throw new NullPointerException("Args must not be null.");
        }
        if (Arrays.asList(data).contains(null)) {
            throw new NullPointerException("Array arg must not contain null.");
        }
        m_nc = snc;
        m_data = data;
    }

    /** {@inheritDoc} */
    @Override
    public Void call() throws Exception {
        try {
            boolean success = true;
            if (!isReConnecting()) {
                try {
                    m_nc.notifyParentExecuteStart();
                } catch (IllegalContextStackObjectException e) {
                    success = false;
                }
            }
            success = success && mainExecute();
            m_nc.notifyParentExecuteFinished(success);
        } catch (Throwable e) {
            m_logger.error("Caught \"" + e.getClass().getSimpleName() 
                    + "\": " + e.getMessage(), e);
            throw new ExecutionException(e);
        }
        return null;
    }

    public abstract boolean isReConnecting();
    public abstract boolean mainExecute();
    public abstract boolean cancel();

    public PortObject[] getPortObjects() {
        return m_data;
    }

    public NodeContainer getNodeContainer() {
        return m_nc;
    }

}
