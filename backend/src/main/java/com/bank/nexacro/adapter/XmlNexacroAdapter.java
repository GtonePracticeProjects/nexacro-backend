package com.bank.nexacro.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 벤더 jar 없이 JDK 만으로 Nexacro "PlatformXml" 포맷을 읽고 쓰는 기본 구현.
 * (클라이언트 transaction 의 데이터 타입이 XML(기본값) 일 때만 동작. SSV/Binary 는 미지원)
 *
 * <p>주의: Nexacro 런타임과 붙여서 검증하지 않았다. 실제 운영에서는 벤더 제공 어댑터로 교체하는 것을 전제로 한다.</p>
 */
@Component
public class XmlNexacroAdapter implements NexacroAdapter {

    private static final String NS = "http://www.nexacroplatform.com/platform/dataset";

    @Override
    public NexacroRequest read(HttpServletRequest request) throws IOException {
        NexacroRequest req = new NexacroRequest();
        InputStream in = request.getInputStream();
        Document doc;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder b = f.newDocumentBuilder();
            doc = b.parse(in);
        } catch (Exception e) {
            throw new IOException("Nexacro XML 요청을 해석할 수 없습니다: " + e.getMessage(), e);
        }
        Element root = doc.getDocumentElement();
        for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            if ("Parameters".equals(e.getTagName())) {
                readParameters(e, req);
            } else if ("Dataset".equals(e.getTagName())) {
                NexacroDataset ds = readDataset(e);
                req.getDatasets().put(ds.getId(), ds);
            }
        }
        return req;
    }

    private void readParameters(Element params, NexacroRequest req) {
        NodeList list = params.getElementsByTagName("Parameter");
        for (int i = 0; i < list.getLength(); i++) {
            Element p = (Element) list.item(i);
            req.getVariables().put(p.getAttribute("id"), p.getTextContent());
        }
    }

    private NexacroDataset readDataset(Element e) {
        NexacroDataset ds = new NexacroDataset(e.getAttribute("id"));
        NodeList cols = e.getElementsByTagName("Column");
        for (int i = 0; i < cols.getLength(); i++) {
            Element c = (Element) cols.item(i);
            ds.addColumn(c.getAttribute("id"), c.getAttribute("type").toUpperCase());
        }
        NodeList rows = e.getElementsByTagName("Row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element r = (Element) rows.item(i);
            Map<String, Object> row = ds.addRow();
            NodeList vals = r.getElementsByTagName("Col");
            for (int j = 0; j < vals.getLength(); j++) {
                Element c = (Element) vals.item(j);
                row.put(c.getAttribute("id"), c.getTextContent());
            }
        }
        return ds;
    }

    @Override
    public void write(HttpServletResponse response, NexacroResponse result) throws IOException {
        response.setContentType("text/xml;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Writer w = response.getWriter();
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write("<Root xmlns=\"" + NS + "\">\n<Parameters>\n");
        param(w, "ErrorCode", "int", String.valueOf(result.getErrorCode()));
        param(w, "ErrorMsg", "string", result.getErrorMsg());
        for (Map.Entry<String, String> v : result.getVariables().entrySet()) {
            param(w, v.getKey(), "string", v.getValue());
        }
        w.write("</Parameters>\n");
        for (NexacroDataset ds : result.getDatasets().values()) {
            writeDataset(w, ds);
        }
        w.write("</Root>");
        w.flush();
    }

    private void writeDataset(Writer w, NexacroDataset ds) throws IOException {
        w.write("<Dataset id=\"" + esc(ds.getId()) + "\">\n<ColumnInfo>\n");
        for (Map.Entry<String, String> c : ds.getColumns().entrySet()) {
            w.write("<Column id=\"" + esc(c.getKey()) + "\" type=\"" + esc(c.getValue().toLowerCase())
                    + "\" size=\"256\"/>\n");
        }
        w.write("</ColumnInfo>\n<Rows>\n");
        for (Map<String, Object> row : ds.getRows()) {
            w.write("<Row>");
            for (String col : ds.getColumns().keySet()) {
                Object v = row.get(col);
                if (v != null) {
                    w.write("<Col id=\"" + esc(col) + "\">" + esc(v.toString()) + "</Col>");
                }
            }
            w.write("</Row>\n");
        }
        w.write("</Rows>\n</Dataset>\n");
    }

    private void param(Writer w, String id, String type, String value) throws IOException {
        w.write("<Parameter id=\"" + esc(id) + "\" type=\"" + type + "\">" + esc(value == null ? "" : value)
                + "</Parameter>\n");
    }

    private String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
