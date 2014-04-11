package com.github.onlysavior.jtrace.analyse.offline;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-10
 * Time: 下午6:01
 * To change this template use File | Settings | File Templates.
 */
public class UrlNorm {

    public String norm(String url) {
        try {
            URL u = new URL(url);
            StringBuilder sb = new StringBuilder();
            sb.append(u.getProtocol()).append("://")
                    .append(u.getHost()).append(u.getPath());
            return sb.toString();
        } catch (MalformedURLException e) {
            return url;
        }
    }

    public static void main(String[] args) {
        String url = "https://login.taobao.com/login?username=aaa&password=bbb";
        UrlNorm norm = new UrlNorm();
        norm.norm(url);
    }
}
