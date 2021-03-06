/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.server.identity;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.SignatureData;
import org.eclipse.milo.opcua.stack.core.types.structured.UserIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite {@link IdentityValidator} that tries its component {@link IdentityValidator}s in the order provided.
 */
public class CompositeValidator implements IdentityValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ImmutableList<IdentityValidator> validators;

    public CompositeValidator(IdentityValidator... validators) {
        this.validators = ImmutableList.copyOf(validators);
    }

    public CompositeValidator(List<IdentityValidator> validators) {
        this.validators = ImmutableList.copyOf(validators);
    }

    @Override
    public Object validateIdentityToken(
        Session session,
        UserIdentityToken token,
        UserTokenPolicy tokenPolicy,
        SignatureData tokenSignature) throws UaException {

        Iterator<IdentityValidator> iterator = validators.iterator();

        while (iterator.hasNext()) {
            IdentityValidator validator = iterator.next();

            try {
                return validator.validateIdentityToken(session, token, tokenPolicy, tokenSignature);
            } catch (Exception e) {
                if (!iterator.hasNext()) {
                    throw e;
                }

                logger.debug("IdentityValidator={} failed, trying next...", validator.toString());
            }
        }

        throw new UaException(StatusCodes.Bad_IdentityTokenInvalid);
    }

}
