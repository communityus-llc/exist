package org.exist.xquery;

import org.exist.dom.NodeProxy;
import org.exist.dom.QName;
import org.exist.xquery.value.Type;
import org.w3c.dom.Node;

public class NameTest extends TypeTest {

	protected QName nodeName;

	public NameTest(int type, QName name) {
		super(type);
		nodeName = name;
	}

	public QName getName() {
		return nodeName;
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.NodeTest#matches(org.exist.dom.NodeProxy)
	 */
	public boolean matches(NodeProxy proxy) {
		Node node = null;
		short type = proxy.nodeType;
		if(proxy.getType() == Type.ITEM) {
			node = proxy.getNode();
			type = node.getNodeType();
		}
		if (!isOfType(type))
			return false;
		if(node == null)
			node = proxy.getNode();
		return matchesName(node);
	}

	public boolean matches(Node other) {
		if(!isOfType(other.getNodeType()))
			return false;
		return matchesName(other);
	}
	
	public boolean matchesName(Node other) {
		if (nodeName.getNamespaceURI() != null) {
			if (!nodeName.getNamespaceURI().equals(other.getNamespaceURI()))
				return false;
		}
		if (nodeName.getLocalName() != null) {
			return nodeName.getLocalName().equals(other.getLocalName());
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.NodeTest#isWildcardTest()
	 */
	public boolean isWildcardTest() {
		return nodeName.getLocalName() == null || nodeName.getNamespaceURI() == null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    if(nodeName.getLocalName() == null)
	        return nodeName.getPrefix() + ":*";
	    else
	        return nodeName.toString();
	}

}
