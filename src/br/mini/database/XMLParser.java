package br.mini.database;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import br.mini.Constantes;
import br.mini.exception.MiniException;

/**
 * Classe responsável por fazer o Parser do xml de configuração.
 *
 * @author figueiredo-lucas
 *
 */
class XMLParser {

    /**
     * Método responsável por obter os parâmetros mapeados no xml
     *
     * @return mapa com parâmetros e valores
     * @throws MiniException
     */
    static Map<String, String> getMapParams() throws MiniException {
        String xml = "/mini.cfg.xml";
        URL resource = Thread.currentThread().getContextClassLoader().getResource(xml);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Map<String, String> mapaConn = new HashMap<String, String>();

        try {
            docBuilder = dbf.newDocumentBuilder();
            Document doc;
            doc = docBuilder.parse(new File(resource.toURI()));
            NodeList nl = doc.getElementsByTagName("property");
            for (int i = 0; i < nl.getLength(); i++) {
                mapaConn.put(nl.item(i).getAttributes().getNamedItem("name").getNodeValue(), nl.item(i).getTextContent());
            }
            return mapaConn;
        } catch (Exception ex) {
            throw new MiniException(Constantes.XML_INVALIDO, ex);
        }
    }

}
