package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.core.Jtrace;
import com.github.onlysavior.jtrace.core.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 下午10:23
 * To change this template use File | Settings | File Templates.
 */
public class CallPathMaker {
    private static final UrlNorm urlNorm = new UrlNorm();

    public CallPath.CallNode make(List<CallPath.CallNode> nodes) {
        List<CallPath.CallNode> tmp = new LinkedList<CallPath.CallNode>(nodes);
        int len = tmp.size();
        int max = len + 1000;
        int count = 0;
        Queue<CallPath.CallNode> queue = new LinkedList<CallPath.CallNode>();
        CallPath.CallNode root = except(tmp, Jtrace.ROOT_PARENT_SPAN_ID, null, queue);
        while (queue.size() > 0 && count++ < max) {
            CallPath.CallNode peek = queue.poll();
            except(tmp, peek.getSpanId(), peek, queue);
        }
        return root;
    }

    private CallPath.CallNode except(List<CallPath.CallNode> tmp,
                                     String toFind,
                                     CallPath.CallNode parent,
                                     Queue<CallPath.CallNode> queue) {
        Iterator<CallPath.CallNode> iterator = tmp.iterator();
        CallPath.CallNode rtn = null;
        while (iterator.hasNext()) {
            CallPath.CallNode node = iterator.next();
            if (node.getParentSpanId().equals(toFind)) {
                node.setParent(parent);
                if (parent != null) {
                    node.setPath(addPath(parent.getPath(), node));
                    parent.addChild(node);
                } else {
                    node.setPath(urlNorm.norm(node.getTraceName()));
                }
                queue.add(node);
                rtn = node;
                iterator.remove();
            }
        }

        return rtn;
    }

    private String addPath(String parentPath, CallPath.CallNode currentNode) {
        StringBuilder sb = new StringBuilder(parentPath);
        sb.append("|").append(currentNode.getServerName());
        return sb.toString();
    }
}
