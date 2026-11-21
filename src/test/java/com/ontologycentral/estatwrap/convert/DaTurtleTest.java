package com.ontologycentral.estatwrap.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

/** /da/{id} Turtle from a filtered Eurostat answer (recorded 2026-11-21). No network. */
public class DaTurtleTest {

    static final String TSV = "freq,unit,sex,age,geo\\TIME_PERIOD\t2023 \t2024 \n"
            + "A,NR,T,TOTAL,DE254\t523734 b\t526606 \n"
            + "A,NR,T,TOTAL,DE255\t40812 b\t40994 \n";

    static final String DOC = "https://estatwrap.example/da/demo_r_pjanaggr3";
    static final String UPSTREAM = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/data/dataflow/ESTAT/"
            + "demo_r_pjanaggr3/1.0?format=tsv&compress=false&c%5Bgeo%5D=DE254%2CDE255";

    private static String turtle() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Da.convertWithSdmx3IdentifiersToTurtle(os, null, "demo_r_pjanaggr3", new StringReader(TSV), UPSTREAM);
        return os.toString(StandardCharsets.UTF_8);
    }

    private static Model parse(String ttl, String base) {
        Model m = ModelFactory.createDefaultModel();
        m.read(new StringReader(ttl), base, "TURTLE");
        return m;
    }

    @Test
    public void filteredViewObservationsBelongToTheWholeDataset() throws Exception {
        Model m = parse(turtle(), DOC + "?geo=DE254,DE255");
        Resource ds = m.getResource(DOC + "#ds");
        assertTrue(m.contains(ds, RDF.type, m.getResource("http://purl.org/linked-data/cube#DataSet")));
        assertEquals(4, m.listSubjectsWithProperty(
                m.getProperty("http://purl.org/linked-data/cube#dataSet"), ds).toList().size());
        assertTrue(m.contains(null, m.getProperty("http://www.w3.org/ns/prov#used"), m.getResource(UPSTREAM)));
    }

    @Test
    public void templateHasOneMappingPerDimensionWithWorkingExamples() throws Exception {
        Model m = parse(turtle(), DOC);
        String q = "PREFIX hydra: <http://www.w3.org/ns/hydra/core#> PREFIX sh: <http://www.w3.org/ns/shacl#>"
                + " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
                + " SELECT ?t ?v ?p ?ex ?prop WHERE { <" + DOC + "#ds> hydra:search ?s . ?s a hydra:IriTemplate ;"
                + " hydra:template ?t ; hydra:mapping ?mp ; hydra:operation ?op . ?op hydra:method \"GET\" ."
                + " ?mp hydra:variable ?v ; hydra:required false ; sh:pattern ?p ; skos:example ?ex ;"
                + " hydra:property ?prop }";
        Set<String> vars = new HashSet<>();
        try (var qe = QueryExecutionFactory.create(q, m)) {
            var rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution s = rs.next();
                assertEquals("demo_r_pjanaggr3{?freq,unit,sex,age,geo,TIME_PERIOD}",
                        s.getLiteral("t").getString());
                String ex = s.getLiteral("ex").getString();
                assertTrue(ex + " fails its own pattern",
                        Pattern.matches(s.getLiteral("p").getString(), ex));
                vars.add(s.getLiteral("v").getString());
                assertEquals("https://estatwrap.example/ds/demo_r_pjanaggr3#dim-" + s.getLiteral("v").getString(),
                        s.getResource("prop").getURI());
            }
        }
        assertEquals(Set.of(List.of("freq", "unit", "sex", "age", "geo", "TIME_PERIOD").toArray()), vars);
    }

    @Test
    public void hostNeverAppears() throws Exception {
        assertFalse(turtle().contains("estatwrap.example"));
    }
}
