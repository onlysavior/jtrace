package com.github.onlysavior.jtrace.analyse;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午3:40
 * To change this template use File | Settings | File Templates.
 */
public class CallLink implements Serializable,Iterable<CallLink.CallLinkEntry> {
    public String entryURL;
    public long entrySign;
    public List<CallLinkEntry> entries = new LinkedList<CallLinkEntry>();

    public void addEntry(CallLinkEntry e) {
        entries.add(e);
    }

    @Override
    public Iterator<CallLinkEntry> iterator() {
        return entries.iterator();
    }

    public static class CallLinkEntry implements Serializable {
        public String serverName;
        public long nodeSign;
        public String pathFromEntry;
    }
}
