/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.xquery.functions.util;

import org.exist.dom.DocumentSet;
import org.exist.dom.NodeSet;
import org.exist.dom.QName;
import org.exist.storage.Indexable;
import org.exist.util.ValueOccurrences;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionReference;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * @author wolf
 *
 */
public class IndexKeys extends BasicFunction {

    public final static FunctionSignature signature = new FunctionSignature(
            new QName("index-keys", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
            "This function can be used to collect some information on the distribution " +
            "of index terms within a set of nodes. The set of nodes is specified in the first " +
            "argument $a. The function returns term frequencies for all terms in the index found " +
            "in descendants of the nodes in $a. The second argument $b specifies " +
            "a start string. Only terms starting with the specified character sequence are returned. " +
            "$c is a function reference, which points to a callback function that will be called " +
            "for every term occurrence. $d defines the maximum number of terms that should be " +
            "reported. The function reference for $c can be created with the util:function " +
            "function. It can be an arbitrary user-defined function, but it should take exactly 2 arguments: " +
            "1) the current term as found in the index as xs:string, 2) a sequence containing three int " +
            "values: a) the overall frequency of the term within the node set, b) the number of distinct " +
            "documents in the node set the term occurs in, c) the current position of the term in the whole " +
            "list of terms returned.",
            new SequenceType[]{
                    new SequenceType(Type.NODE, Cardinality.ZERO_OR_MORE),
                    new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE),
                    new SequenceType(Type.FUNCTION_REFERENCE, Cardinality.EXACTLY_ONE),
                    new SequenceType(Type.INT, Cardinality.EXACTLY_ONE)
            },
            new SequenceType(Type.ITEM, Cardinality.ZERO_OR_MORE));

    /**
     * @param context
     * @param signature
     */
    public IndexKeys(XQueryContext context) {
        super(context, signature);
    }

    /* (non-Javadoc)
     * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
     */
    public Sequence eval(Sequence[] args, Sequence contextSequence)
            throws XPathException {
        if(args[0].getLength() == 0)
            return Sequence.EMPTY_SEQUENCE;
        NodeSet nodes = args[0].toNodeSet();
        DocumentSet docs = nodes.getDocumentSet();
        String start = args[1].getStringValue();
        FunctionReference ref = (FunctionReference) args[2].itemAt(0);
        int max = ((IntegerValue) args[3].itemAt(0)).getInt();
        FunctionCall call = ref.getFunctionCall();
        Sequence result = new ValueSequence();
        ValueOccurrences occur[] = 
            context.getBroker().getValueIndex().scanIndexTerms(docs, nodes, (Indexable) args[1], null);
        int len = (occur.length > max ? max : occur.length);
        Sequence params[] = new Sequence[2];
        ValueSequence data = new ValueSequence();
        for (int j = 0; j < len; j++) {
            params[0] = occur[j].getValue();
            data.add(new IntegerValue(occur[j].getOccurrences(), Type.UNSIGNED_INT));
            data.add(new IntegerValue(occur[j].getDocuments(), Type.UNSIGNED_INT));
            data.add(new IntegerValue(j + 1, Type.UNSIGNED_INT));
            params[1] = data;
            
            result.addAll(call.evalFunction(contextSequence, null, params));
            data.clear();
        }
        LOG.debug("Returning: " + result.getLength());
        return result;
    }

}
