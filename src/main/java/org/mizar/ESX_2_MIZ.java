package org.mizar;

import java.io.*;
import java.util.*;

import org.dom4j.*;

public class ESX_2_MIZ extends XMLApplication {

    public int errorNbr;
    public int argsLevel = -1;

    public Set<String> errors = new TreeSet<>();

    public File outFile;
    public String newExtension;
    public String newFileName;
    public String outString = "";
    public int textLevel = 0;

    public ESX_2_MIZ(String fileName, String newExtension) {
        super(fileName);
        this.newExtension = newExtension;
        this.newFileName = this.getFileName().getCanonicalFileNameWithoutExtension() + newExtension;
        outFile = new File(newFileName);
    }

    public void p(String s) {
        outString += s + " ";
    }

    public void n(String s) {
        outString += "\n";
        spaces(2 * textLevel);
        p(s);
    }

    public void pln(String s) {
        outString += s + "\n";
    }

    public void mainItem(String s) {
        p("\n\n" + s);
    }

    public void spaces(int nbr) {
        String res = "";
        for (int i = 1; i <= nbr; i++)
            res += " ";
        outString += res;
    }

    public void error(Element e, String kind) {
//        p("UNKNOWN " + (kind.toUpperCase()) + " " + e.getName() + "\n");
        errorNbr++;
        errors.add(kind + " " + e.getName());
    }

    public void checkXMLAttribute(Element e, String attrName) {
        if (e.attribute(attrName) == null) {
            error(e,"UNKNOWN XML ATTRIBUTE EXCEPTION");
            throw new UnknownAttributeException(attrName);
        }
    }

    public void spelling(Element e) {
        checkXMLAttribute(e, ESXAttributeName.SPELLING);
        p(e.attributeValue(ESXAttributeName.SPELLING));
    }

    public void processSpellingList(Element e) {
        List<Node> nodes = e.selectNodes("*");
        if (nodes.size() == 0)
            return;
        int i = 0;
        for (i = 0; i < nodes.size() - 1; i++) {
            p(nodes.get(i).valueOf("@spelling"));
            p(",");
        }
        p(nodes.get(i).valueOf("@spelling"));
    }

    public void processTextProper() {
    }

    public void addTheoremReference(Element e, String kind) {
        checkXMLAttribute(e,"MMLId");
        String[] tab = e.valueOf("@MMLId").split(":");
        p(tab[0] + ":" + kind + " " + tab[1]);
    }

    public void addSchemeReference(Element e) {
        checkXMLAttribute(e,"MMLId");
        String[] tab = e.valueOf("@MMLId").split(":");
        p(tab[0] + ":sch " + tab[1]);
    }

    public void addLocalSchemeReference(Element e) {
        spelling(e);
    }

    public void addNameWithoutSpelling(Element e) {
    }

    public void addPrivateName(Element e) {
        spelling(e);
    }

    public void addLabelName(Element e) {
        spelling(e);
    }

    public void processAdjective(Element e) {
        if (e.valueOf("@nonocc").equals("true"))
            p("non");
        processTermList(e.element("Arguments"));
        spelling(e);
    }

    public void processArgumentsMode(List<Node> args, String of) {
        if (args.size() > 0) {
            p(of);
            int i = 0;
            for (i = 0; i < args.size() - 1; i++) {
                processTerm((Element) args.get(i));
                p(",");
            }
            processTerm((Element) args.get(i));
        }
    }

    public void processStandardType(Element e) {
        spelling(e);
        processArgumentsMode(e.selectNodes("Arguments/*"), "of");
    }

    public void processStructType(Element e) {
        spelling(e);
        processArgumentsMode(e.selectNodes("Arguments/*"), "over");
    }

    public void processType(Element e) {
        checkXMLAttribute(e,"sort");
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed) {
            p("(");
        }
        switch (e.getName()) {
            case "Standard-Type":
                processStandardType(e);
                break;
            case "Struct-Type":
                processStructType(e);
                break;
            case "Clustered-Type":
                processClusteredType(e);
                break;
            default:
                error(e, "TYPE");
        }
        if (bracketed) {
            p(")");
        }
    }

    public void processExplicitlyQualifiedSegment(Element e, String qualifier) {
        processVariables(e);
        p(qualifier);
        processType((Element) e.selectSingleNode("*[2]"));
    }

    public void processExplicitlyQualifiedSegment(Element e) {
        processExplicitlyQualifiedSegment(e,"be");
    }

    public void processImplicitlyQualifiedSegment(Element e) {
        processVariable(e.element("Variable"));
    }

    public void processVariableSegment(Element e, String qualifier) {
        switch (e.getName()) {
            case "Explicitly-Qualified-Segment":
                processExplicitlyQualifiedSegment(e,qualifier);
                break;
            case "Implicitly-Qualified-Segment":
                processImplicitlyQualifiedSegment(e);
                break;
            case "Free-Variable-Segment":
                break;
            default:
                error(e, "VARIABLE SEGMENT");
        }
    }

    public void processVariableSegment(Element e) {
        processVariableSegment(e,"be");
    }

    public void addStartOfSegments(Element e, String word) {
        if (e.selectNodes("./Implicitly-Qualified-Segment | ./Explicitly-Qualified-Segment").size() != 0)
            p(word);
    }

    public void processVariableSegments(Element e, String qualifier) {
        List<Node> segments = e.selectNodes("./Implicitly-Qualified-Segment | ./Explicitly-Qualified-Segment");
        if (segments.isEmpty())
            return;
        int i = 0;
        for (i = 0; i < segments.size() - 1; i++) {
            if (qualifier.equals("is")) {
                addStartOfSegments(e, "where");
            }
            processVariableSegment((Element) segments.get(i),qualifier);
            if (!qualifier.equals("is")) {
                p(",");
            }
        }
        if (qualifier.equals("is")) {
            addStartOfSegments(e, "where");
        }
        processVariableSegment((Element) segments.get(i),qualifier);
    }

    public void processVariableSegments(Element e) {
        processVariableSegments(e,"be");
    }

    public void processExistentialFormula(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        p("ex");
        processVariableSegments((Element) e.selectSingleNode("./Variable-Segments"));
        p("st");
        processScope(e.element("Scope"));
        if (bracketed)
            p(")");
    }

    public void processRelationFormula(Element e) {
        checkXMLAttribute(e,"leftargscount");
        int leftargsnr = Integer.parseInt(e.valueOf("@leftargscount"));
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        processArguments(e, leftargsnr, false);
        if (bracketed)
            p(")");
    }

    public void processQualifyingFormula(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        processTerm((Element) e.selectSingleNode("./*[1]"));
        p("is");
        processType((Element) e.selectSingleNode("./*[2]"));
        if (bracketed)
            p(")");
    }

    public void processAdjectiveCluster(Element e) {
        for (Node node : e.selectNodes("Attribute"))
            processAdjective((Element) node);
    }

    public void processMultiAttributiveFormula(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        processTerm((Element) e.selectSingleNode("./*[1]"));
        p("is");
        processAdjectiveCluster((Element) e.selectSingleNode("./*[2]"));
        if (bracketed)
            p(")");
    }

    public void processRightSideFormula(Element e) {
        checkXMLAttribute(e,"leftargscount");
        spelling(e);
        processRightSideArguments(e, Integer.parseInt(e.valueOf("@leftargscount")));
    }

    public void processMultiRelationFormula(Element e) {
        processFormula(e.elements().get(0));
        List<Element> rights = e.elements("RightSideOf-Relation-Formula");
        int i = 0;
        for (i = 0; i < rights.size(); i++) {
            processRightSideFormula(rights.get(i));
        }
    }

    public void processCorrectnessCondition(Element e) {
        n(e.selectSingleNode("./*[1]").getName());
        processJustification((Element) e.selectSingleNode("./*[2]"));
    }

    public void processCorrectness(Element e) {
        n("correctness");
        processJustification((Element) e.selectSingleNode("./*[2]"));
    }

    public void processProperty(Element e) {
        checkXMLAttribute(e.element("Properties"),"property");
        n(e.element("Properties").valueOf("@property"));
        processJustification((Element) e.selectSingleNode("./*[2]"));
    }

    public void processScope(Element e) {
        processFormula(e.elements().get(0));
    }

    public void processUniversalQuantifierFormula(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        Element segments = e.element("Variable-Segments");
        addStartOfSegments(segments,"for");
        processVariableSegments(segments);
        if (e.selectSingleNode("./*[2]").getName().equals("Restriction")) {
            p("st");
            processFormula((Element) e.selectSingleNode("./*[2]/*[1]"));
            p("holds");
            processScope(e.element("Scope"));
        } else {
            addStartOfSegments(segments,"holds");
            processScope(e.element("Scope"));
        }
        if (bracketed)
            p(")");
    }

    public void processNegatedFormula(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        p("not");
        processFormula(e.elements().get(0));
        if (bracketed)
            p(")");
    }

    public void processBinaryFormula(Element e, String connective) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed)
            p("(");
        processFormula(e.elements().get(0));
        p(connective);
        processFormula(e.elements().get(1));
        if (bracketed)
            p(")");
    }

    public void processConjunctiveFormula(Element e) {
        processBinaryFormula(e,"&");
    }

    public void processDisjunctiveFormula(Element e) {
        processBinaryFormula(e,"or");
    }

    public void processConditionalFormula(Element e) {
        processBinaryFormula(e,"implies");
    }

    public void processBiconditionalFormula(Element e) {
        processBinaryFormula(e,"iff");
    }

    public void processFlexaryDisjunctiveFormula(Element e) {
        processBinaryFormula(e,"or ... or");
    }

    public void processFlexaryConjunctiveFormula(Element e) {
        processBinaryFormula(e,"& ... &");
    }

    public void processPrivatePredicateFormulaName(Element e) {
        spelling(e);
    }

    public void processPrivatePredicateFormula(Element e) {
        processPrivatePredicateFormulaName(e);
        p("[");
        processTermList(e.element("Arguments"));
        p("]");
    }

    public void processFormula(Element e) {
        switch (e.getName()) {
            case "Existential-Quantifier-Formula":
                processExistentialFormula(e);
                break;
            case "Universal-Quantifier-Formula":
                processUniversalQuantifierFormula(e);
                break;
            case "Relation-Formula":
                processRelationFormula(e);
                break;
            case "Qualifying-Formula":
                processQualifyingFormula(e);
                break;
            case "Multi-Attributive-Formula":
                processMultiAttributiveFormula(e);
                break;
            case "Multi-Relation-Formula":
                processMultiRelationFormula(e);
                break;
            case "Negated-Formula":
                processNegatedFormula(e);
                break;
            case "Conjunctive-Formula":
                processConjunctiveFormula(e);
                break;
            case "Disjunctive-Formula":
                processDisjunctiveFormula(e);
                break;
            case "Conditional-Formula":
                processConditionalFormula(e);
                break;
            case "Biconditional-Formula":
                processBiconditionalFormula(e);
                break;
            case "FlexaryDisjunctive-Formula":
                processFlexaryDisjunctiveFormula(e);
                break;
            case "FlexaryConjunctive-Formula":
                processFlexaryConjunctiveFormula(e);
                break;
            case "Thesis":
                p("thesis");
                break;
            case "Contradiction":
                p("contradiction");
                break;
            case "Private-Predicate-Formula":
                processPrivatePredicateFormula(e);
                break;
            default:
                error(e, "FORMULA");
        }
    }

    public void processTypeList(Element e) {
        List<Node> types = e.selectNodes("./*");
        if (types.size() == 0) return;
        int i = 0;
        for (i = 0; i < types.size() - 1; i++) {
            processType((Element) types.get(i));
            p(",");
        }
        processType((Element) types.get(i));
    }

    public void processTermList(Element e) {
        List<Node> terms = e.selectNodes("./*");
        if (terms.size() == 0) return;
        int i = 0;
        for (i = 0; i < terms.size() - 1; i++) {
            processTerm((Element) terms.get(i));
            p(",");
        }
        processTerm((Element) terms.get(i));
    }

    public boolean nullaryOperation(Element e) {
        if (e.getName().equals("Infix-Term")) {
            return e.element("Arguments").elements().size() == 0;
        }
        return false;
    }

    public boolean argsLevelZero(Element e) {
        return argsLevel == 0 || e.attribute("bracketed") != null;
    }

    public boolean addBracketsInTerms(Element e) {
        return ! e.getName().equals("Numeral-Term") &&
                ! e.getName().equals("Simple-Term") &&
                ! e.getName().equals("it-Term") &&
                ! e.getName().equals("Placeholder-Term") &&
                ! e.getName().equals("Private-Functor-Term") &&
                ! e.getName().equals("Circumfix-Term") &&
                ! e.getName().equals("Fraenkel-Term") &&
                ! e.getName().equals("Simple-Fraenkel-Term") &&
                ! nullaryOperation(e) &&
                ! argsLevelZero(e)
                ;
    }

    public void processArguments(Element e, int leftargsnr, boolean with_brackets) {
        List<Node> args = e.selectNodes("./Arguments/*");
        argsLevel++;
        int i = 0;
        if (with_brackets && leftargsnr > 1)
            p("(");
        if (leftargsnr > 0) {
            for (i = 0; i < leftargsnr - 1; i++) {
                if (addBracketsInTerms((Element)args.get(i))) {
                    p("(");
                }
                processTerm((Element) args.get(i));
                if (addBracketsInTerms((Element)args.get(i))) {
                    p(")");
                }
                p(",");
            }
            if (addBracketsInTerms((Element)args.get(i))) {
                p("(");
            }
            processTerm((Element) args.get(i));
            if (addBracketsInTerms((Element)args.get(i))) {
                p(")");
            }
        }
        if (with_brackets && leftargsnr > 1)
            p(")");
        spelling(e);
        if (with_brackets && args.size() - leftargsnr > 1)
            p("(");
        if (args.size() - leftargsnr > 0) {
            for (i = leftargsnr; i < args.size() - 1; i++) {
                if (addBracketsInTerms((Element)args.get(i))) {
                    p("(");
                }
                processTerm((Element) args.get(i));
                if (addBracketsInTerms((Element)args.get(i))) {
                    p(")");
                }
                p(",");
            }
            if (addBracketsInTerms((Element)args.get(i))) {
                p("(");
            }
            processTerm((Element) args.get(i));
            if (addBracketsInTerms((Element)args.get(i))) {
                p(")");
            }
        }
        if (with_brackets && args.size() - leftargsnr > 1)
            p(")");
        argsLevel--;
    }

    public void processRightSideArguments(Element e, int leftargsnr) {
        List<Node> args = e.selectNodes("./Arguments/*");
        int i = 0;
        if (args.size() - leftargsnr > 0) {
            for (i = leftargsnr; i < args.size() - 1; i++) {
                processTerm((Element) args.get(i));
                p(",");
            }
            processTerm((Element) args.get(i));
        }
    }

    public void processPrivateFunctorTermName(Element e) {
        spelling(e);
    }

    public void processPrivateFunctorTerm(Element e) {
        processPrivateFunctorTermName(e);
        p("(");
        processTermList(e.element("Arguments"));
        p(")");
    }

    public void processPlaceholderTerm(Element e) {
        spelling(e);
    }

    public void processGlobalChoiceTerm(Element e) {
        p("the");
        processType(e.elements().get(0));
    }

    public void processSelectorTerm(Element e) {
        p("the");
        spelling(e);
        p("of");
        processTermList(e);
    }

    public void processAggregateTerm(Element e) {
        spelling(e);
        p("(#");
        processTermList(e.element("Arguments"));
        p("#)");
    }

    public void processQualificationTerm(Element e) {
        processTerm(e.elements().get(0));
        p("qua");
        processType(e.elements().get(1));
    }

    public void processInternalSelectorTerm(Element e) {
        p("the");
        spelling(e);
    }

    public void processForgetfulFunctorTerm(Element e) {
        p("the");
        spelling(e);
        p("of");
        processTerm(e.elements().get(0));
    }

    public void processFraenkelTerm(Element e) {
        p("{");
        processTerm(e.elements().get(1));
        processVariableSegments(e.element("Variable-Segments"),"is");
        p(":");
        processFormula(e.elements().get(2));
        p("}");
    }

    public void processSimpleFraenkelTerm(Element e) {
        p("the set of all");
        processTerm(e.elements().get(1));
        processVariableSegments(e.element("Variable-Segments"),"is");
    }

    public void processInfixTerm(Element e) {
        checkXMLAttribute(e,"leftargscount");
        int leftargsnr = Integer.parseInt(e.valueOf("@leftargscount"));
        processArguments(e, leftargsnr, true);
    }

    public void processCircumfixTerm(Element e) {
        spelling(e);
        processTermList(e.element("Arguments"));
        spelling(e.element("Right-Circumflex-Symbol"));
    }

    public void processItTerm(Element e) {
        p("it");
    }

    public void processNumeralTerm(Element e) {
        checkXMLAttribute(e,"number");
        p(e.valueOf("@number"));
    }

    public void processSimpleTerm(Element e) {
        spelling(e);
    }

    public void processTerm(Element e) {
        boolean bracketed = e.attribute("bracketed") != null;
        if (bracketed) {
            p("(");
        }
        switch (e.getName()) {
            case "Simple-Term":
                processSimpleTerm(e);
                break;
            case "Numeral-Term":
                processNumeralTerm(e);
                break;
            case "it-Term":
                processItTerm(e);
                break;
            case "Infix-Term":
                processInfixTerm(e);
                break;
            case "Circumfix-Term":
                processCircumfixTerm(e);
                break;
            case "Private-Functor-Term":
                processPrivateFunctorTerm(e);
                break;
            case "Placeholder-Term":
                processPlaceholderTerm(e);
                break;
            case "Global-Choice-Term":
                processGlobalChoiceTerm(e);
                break;
            case "Selector-Term":
                processSelectorTerm(e);
                break;
            case "Aggregate-Term":
                processAggregateTerm(e);
                break;
            case "Qualification-Term":
                processQualificationTerm(e);
                break;
            case "Internal-Selector-Term":
                processInternalSelectorTerm(e);
                break;
            case "Forgetful-Functor-Term":
                processForgetfulFunctorTerm(e);
                break;
            case "Fraenkel-Term":
                processFraenkelTerm(e);
                break;
            case "Simple-Fraenkel-Term":
                processSimpleFraenkelTerm(e);
                break;
            default:
                error(e, "TERM");
        }
        if (bracketed) {
            p(")");
        }
    }

    public void processSectionItem(Element e) {
        mainItem("begin");
    }

    public boolean processLabel(Element e, boolean definition) {
        checkXMLAttribute(e,"idnr");
        if (!e.valueOf("@idnr").equals("0")) {
            n("");
            if (definition)
                p(":");
            addLabelName(e);
            p(":");
            return true;
        }
        return false;
    }

    public void processReference(Element e) {
        switch (e.getName()) {
            case "Theorem-Reference":
                addTheoremReference(e,"");
                break;
            case "Definition-Reference":
                addTheoremReference(e,"def");
                break;
            case "Scheme-Reference":
                break;
            case "Local-Reference":
                spelling(e);
                break;
            case "Link":
                break;
            default:
                error(e, "REFERENCE");
        }
    }

    public void processStraightforwardJustification(Element e) {
        List<Node> refs = e.selectNodes("./*");
        boolean link = refs != null && refs.size() > 0 && refs.get(0).getName().equals("Link");
        int refsnbr = link ? refs.size() - 2 : refs.size();
        if (refsnbr > 0) {
            p("by");
            int i = 0;
            for (i = refs.size() - refsnbr; i < refs.size() - 1; i++) {
                processReference((Element) refs.get(i));
                p(",");
            }
            processReference((Element) refs.get(i));
        }
    }

    public void processGeneralization(Element e) {
        Element segments = e.element("Qualified-Segments");
        boolean letexists = segments.selectNodes("./Implicitly-Qualified-Segment | ./Explicitly-Qualified-Segment").size() != 0;
        if (letexists)
            n("let");
        Element conditions = (Element) e.selectSingleNode("Conditions");
        if (conditions != null) {
            processVariableSegments((Element) e.selectSingleNode("./Qualified-Segments"));
            p("such that");
            processConditions(conditions);
        } else {
            processVariableSegments((Element) e.selectSingleNode("./Qualified-Segments"));
        }
        if (letexists)
            p(";");
    }

    public void processAssumption(Element e) {
        switch (e.getName()) {
            case "Collective-Assumption":
                p("that");
                processConditions((Element) e.selectSingleNode("./Conditions"));
                break;
            case "Single-Assumption":
                processProposition((Element) e.selectSingleNode("./Proposition"));
                break;
            default:
                error(e, "ASSUMPTION");
        }
    }

    public void processExistentialAssumption(Element e) {
        n("given");
        processVariableSegments((Element) e.selectSingleNode("./Qualified-Segments"));
        p("such that");
        processConditions((Element) e.selectSingleNode("Conditions"));
    }

    public void processConditions(Element e) {
        List<Node> conditions = e.selectNodes("./*");
        int i = 0;
        for (i = 0; i < conditions.size() - 1; i++) {
            processProposition((Element) conditions.get(i));
            p("and");
        }
        processProposition((Element) conditions.get(i));
    }

    public void processChoiceStatement(Element e) {
        List<Node> nodes = e.selectNodes("./*");
        if (isThen((Element) nodes.get(2)))
            n("then");
        n("consider");
        processVariableSegments((Element) e.selectSingleNode("./Qualified-Segments"));
        p("such that");
        processConditions((Element) e.selectSingleNode("Conditions"));
        processJustification((Element) nodes.get(2));
    }

    public void processExample_ExemplifyingVariable(Element e) {
        processVariable(e.elements().get(0));
    }

    public void processExample_ImplicitExemplification(Element e) {
        processTerm(e.elements().get(0));
    }

    public void processExample_Example(Element e) {
        processVariable(e.elements().get(0));
        p("=");
        processTerm(e.elements().get(1));
    }

    public void processExample(Element e) {
        switch (e.getName()) {
            case "ExemplifyingVariable":
                processExample_ExemplifyingVariable(e);
                break;
            case "ImplicitExemplification":
                processExample_ImplicitExemplification(e);
                break;
            case "Example":
                processExample_Example(e);
                break;
            default:
                error(e, "TAKE");
        }
    }

    public void processExemplification(Element e) {
        List<Element> examples = e.elements();
        n("take");
        int i = 0;
        for (i = 0; i < examples.size() - 1; i++) {
            processExample(examples.get(i));
            p(",");
        }
        processExample(examples.get(i));
    }

    public void processIterativeEquality(Element e, boolean conclusion) {
        n("");
        if (isThen(e.elements().get(2))) {
            if (conclusion)
                n("hence");
            else n("then");
        } else {
            if (conclusion)
                n("thus");
        }
        processProposition(e);
        processJustification(e.elements().get(2));
        List<Node> steps = e.selectNodes("./Iterative-Steps-List/Iterative-Step");
        for (Node node : steps) {
            n(".=");
            processTerm((Element) node.selectSingleNode("./*[1]"));
            processJustification((Element) node.selectSingleNode("./*[2]"));
        }
    }

    public void processHerebyConclusion(Element e) {
        n("hereby");
    }

    public void processNowConclusion(Element e) {
        n("thus");
        processLabel(e.element("Label"),false);
        n("now");
    }

    public boolean isThen(Element e) {
        boolean result = false;
        if (e.getName().equals("Straightforward-Justification"))
            if (e.elements().size() > 1)
                if (e.elements().get(0).getName().equals("Link"))
                    result = true;
        return result;
    }

    public void processConclusion(Element e) {
        if (e.elements().get(0).getName().equals("Iterative-Equality")) {
            processIterativeEquality(e.element("Iterative-Equality"), true);
            return;
        }

        if (e.elements().get(1).valueOf("@kind").equals("Hereby-Reasoning")) {
            processHerebyConclusion(e);
            return;
        }

        if (e.elements().get(1).valueOf("@kind").equals("Now-Reasoning")) {
            processNowConclusion(e.elements().get(0));
            return;
        }

        String word = "thus";
        if (e.selectSingleNode("./*[2]").getName().equals("Straightforward-Justification"))
            if (e.selectSingleNode("./*[2]").selectNodes("./*").size() > 0)
                if (e.selectSingleNode("./*[2]/*[1]").getName().equals("Link"))
                    word = "hence";
        n(word);
        processProposition(e.element("Proposition"));
        processJustification(e.elements().get(1));
    }

    public void processProofBlock(Element e, String start) {
        n(start);
    }

    public void processSchemeJustification(Element e) {
        List<Node> refs = e.selectNodes("./*");
        p("from");
        if (e.attribute("spelling") == null) {
            addSchemeReference(e);
        } else {
            addLocalSchemeReference(e);
        }
        if (refs.size() > 0) {
            p("(");
            int i = 0;
            for (i = 0; i < refs.size() - 1; i++) {
                processReference((Element) refs.get(i));
                p(",");
            }
            processReference((Element) refs.get(i));
            p(")");
        }
    }

    public void processJustification(Element e) {
        switch (e.getName()) {
            case "Straightforward-Justification":
                processStraightforwardJustification(e);
                break;
            case "Scheme-Justification":
                processSchemeJustification(e);
                break;
            case "Block":
                processProofBlock(e, "proof");
                break;
            default:
                error(e, "JUSTIFICATION");
        }
    }

    public void processProposition(Element e) {
        boolean labelled = processLabel((Element) e.selectSingleNode("./*[1]"), false);
        if (!labelled) {
            n("");
        }
        processFormula((Element) e.selectSingleNode("./*[2]"));
    }

    public void processDiffuseStatement(Element e) {
        processProofBlock(e, "now");
    }

    public void processRegularStatement(Element e) {
        switch (e.elements().get(0).getName()) {
            case "Proposition":
                if (isThen(e.elements().get(1)))
                    n("then");
                processProposition(e.elements().get(0));
                processJustification(e.elements().get(1));
                break;
            case "Diffuse-Statement":
                processLabel(e.elements().get(0).element("Label"), false);
                processDiffuseStatement(e.elements().get(1));
                break;
            case "Iterative-Equality":
                processIterativeEquality(e.elements().get(0), false);
                break;
            default:
                error(e, "REGULAR STATEMENT");
        }
    }

    public void processTheoremItem(Element e) {
        mainItem("theorem");
        processRegularStatement(e);
    }

    public void processLocus(Element e) {
        spelling(e);
    }

    public void processLociDeclaration(Element e) {
        n("let");
        processVariableSegments((Element) e.selectSingleNode("./Qualified-Segments"));
        if ((Element) e.selectSingleNode("./Conditions") != null) {
            p("such that");
            processConditions((Element) e.selectSingleNode("./Conditions"));
        }
    }

    public void processDefinition(Element e) {
        switch (e.getName()) {
            case "Functor-Definition":
                processFunctorDefinition(e);
                break;
            case "Predicate-Definition":
                processPredicateDefinition(e);
                break;
            default:
                error(e, "DEFINITION");
        }
    }

    public void processDefinitionItem(Element e) {
        switch (e.elements().get(0).valueOf("@kind")) {
            case "Definitional-Block":
                mainItem("definition");
                break;
            case "Registration-Block":
                mainItem("registration");
                break;
            case "Notation-Block":
                mainItem("notation");
                break;
            default:
                error(e, "DEFINITIONAL BLOCK");
        }
    }

    public void processExistentialRegistration(Element e) {
        processAdjectiveCluster(e.element("Adjective-Cluster"));
        p("for");
        processType(e.elements().get(1));
    }

    public void processFunctorialRegistration(Element e) {
        processTerm(e.elements().get(0));
        p("->");
        processAdjectiveCluster(e.elements().get(1));
        if (e.elements().size() > 2) {
            p("for");
            processType(e.elements().get(2));
        }
    }

    public void processConditionalRegistration(Element e) {
        processAdjectiveCluster(e.elements().get(0));
        p("->");
        processAdjectiveCluster(e.elements().get(1));
        p("for");
        processType(e.elements().get(2));
    }

    public void processCluster(Element e) {
        n("cluster");
        switch (e.getName()) {
            case "Existential-Registration":
                processExistentialRegistration(e);
                break;
            case "Functorial-Registration":
                processFunctorialRegistration(e);
                break;
            case "Conditional-Registration":
                processConditionalRegistration(e);
                break;
            default:
                error(e, "CLUSTER");
        }
    }

    public void processSchemeBlockItem(Element e) {
        mainItem("scheme");
    }

    public void processFunctorSegment(Element e) {
        processVariables(e);
        p("(");
        processTypeList((Element) e.selectSingleNode("./Type-List"));
        p(")");
        processSpecification((Element) e.selectSingleNode("./Type-Specification"));
    }

    public void processPredicateSegment(Element e) {
        processVariables(e);
        p("[");
        processTypeList((Element) e.selectSingleNode("./Type-List"));
        p("]");
    }

    public void processSchematicVariable(Element e) {
        switch (e.getName()) {
            case "Functor-Segment":
                processFunctorSegment(e);
                break;
            case "Predicate-Segment":
                processPredicateSegment(e);
                break;
            default:
                error(e, "SEGMENT");
        }
    }

    public void processSchematicVariables(Element e) {
        p("{");
        List<Node> vars = e.selectNodes("./*");
        int i = 0;
        for (i = 0; i < vars.size() - 1; i++) {
            processSchematicVariable((Element) vars.get(i));
            p(",");
        }
        processSchematicVariable((Element) vars.get(i));
        pln("} :");
    }

    public void processProvisionalFormulas(Element e) {
        n("provided");
        List<Node> formulas = e.selectNodes("./Proposition");
        int i = 0;
        for (i = 0; i < formulas.size() - 1; i++) {
            processProposition((Element) formulas.get(i));
            n("and");
        }
        processProposition((Element) formulas.get(i));
    }

    public void processSchemeHead(Element e) {
        spelling(e.element("Scheme"));
        processSchematicVariables((Element) e.selectSingleNode("./Schematic-Variables"));
        processFormula((Element) e.selectSingleNode("./*[3]"));
        Element provisionals = (Element) e.selectSingleNode("./*[4]");
        if (provisionals != null) {
            processProvisionalFormulas(provisionals);
        } else {
        }
    }

    public void processFuncArgs(List<Node> args, boolean bracketed) {
        if (args.size() == 0) return;
        int i = 0;
        if (bracketed)
            p("(");
        for (i = 0; i < args.size() - 1; i++) {
            processVariable((Element)args.get(i));
            p(",");
        }
        processVariable((Element)args.get(i));
        if (bracketed)
            p(")");
    }

    public void processSpecification(Element e) {
        p("->");
        processType((Element) e.selectSingleNode("./*[1]"));
    }

    public void processInfixFunctorPattern(Element e, boolean intro) {
        processFuncArgs(e.selectNodes("./Loci[1]/*"),e.attribute("leftargsbracketed")!=null);
        spelling(e);
        processFuncArgs(e.selectNodes("./Loci[2]/*"),e.attribute("rightargsbracketed")!=null);
    }

    public void processCircumfixFunctorPattern(Element e, boolean intro) {
        spelling(e);
        processSpellingList((Element) e.selectSingleNode("./Loci"));
        spelling(e.element("Right-Circumflex-Symbol"));
    }

    public void processFunctorPattern(Element e, boolean intro) {
        switch (e.getName()) {
            case "InfixFunctor-Pattern":
                processInfixFunctorPattern(e,intro);
                break;
            case "CircumfixFunctor-Pattern":
                processCircumfixFunctorPattern(e,intro);
                break;
            default:
                error(e, "FUNCTOR PATTERN");
        }
    }

    public void processPredicatePattern(Element e, boolean intro) {
        processSpellingList((Element) e.selectSingleNode("./Loci[1]"));
        spelling(e);
        processSpellingList((Element) e.selectSingleNode("./Loci[2]"));
    }

    public void processSimpleDefiniensEquals(Element e) {
        p("equals");
        processLabel(e.element("Label"), true);
        processTerm((Element) e.selectSingleNode("./*[2]"));
    }

    public void processSimpleDefiniensMeans(Element e) {
        p("means");
        processLabel(e.element("Label"), true);
        processFormula((Element) e.selectSingleNode("./*[2]"));
    }

    public void processPartial(Element e, String kind) {
        switch (kind) {
            case "means":
                processFormula(e.elements().get(0));
                p("if");
                processFormula(e.elements().get(1));
                break;
            case "equals":
                processTerm(e.elements().get(0));
                p("if");
                processFormula(e.elements().get(1));
                break;
            default:
                p("UNKNOWN PARTIAL " + kind + "\n");
        }
    }

    public void processOtherwise(Element e, String kind) {
        if (e.selectNodes("./*").size() > 0) {
            n("otherwise");
            switch (kind) {
                case "means":
                    processFormula(e.elements().get(0));
                    break;
                case "equals":
                    processTerm(e.elements().get(0));
                    break;
                default:
                    p("UNKNOWN OTHERWISE " + kind + "\n");
            }
        }
    }

    public void processPartialDefiniensList(Element e, String kind) {
        List<Element> partials = e.elements();
        int i = 0;
        for (i = 0; i < partials.size() - 1; i++) {
            processPartial(partials.get(i), kind);
            pln(",");
        }
        processPartial(partials.get(i), kind);
    }

    public void processDefiniens(Element e) {
        if (e == null)
            return;
        checkXMLAttribute(e,"kind");
        checkXMLAttribute(e,"shape");
        switch (e.valueOf("@kind")) {
            case "Simple-Definiens":
                switch (e.valueOf("@shape")) {
                    case "Term-Expression":
                        processSimpleDefiniensEquals(e);
                        break;
                    case "Formula-Expression":
                        processSimpleDefiniensMeans(e);
                        break;
                    default:
                        p("UNKNOWN SHAPE " + e.valueOf("@shape") + "\n");
                }
                break;
            case "Conditional-Definiens":
                switch (e.valueOf("@shape")) {
                    case "Term-Expression":
                        p("equals");
                        processLabel(e.element("Label"), true);
                        processPartialDefiniensList(e.element("Partial-Definiens-List"), "equals");
                        processOtherwise(e.element("Otherwise"), "equals");
                        break;
                    case "Formula-Expression":
                        p("means");
                        processLabel(e.element("Label"), true);
                        processPartialDefiniensList(e.element("Partial-Definiens-List"), "means");
                        processOtherwise(e.element("Otherwise"), "means");
                        break;
                    default:
                        p("UNKNOWN SHAPE " + e.valueOf("@shape") + "\n");
                }
                break;
            default:
                p("UNKNOWN DEFINIENS KIND " + e.valueOf("@shape") + "\n");
        }
    }

    public void processFunctorDefinition(Element e) {
        processRedefine(e.element("Redefine"),(Element) e.selectSingleNode("./*[2]"));
        n("func");
        processFunctorPattern((Element) e.selectSingleNode("./*[2]"),true);
        Element specification = (Element) e.selectSingleNode("./Type-Specification");
        if (specification != null)
            processSpecification(specification);
        processDefiniens(e.element("Definiens"));
    }

    public void processRedefine(Element e, Element pattern) {
        checkXMLAttribute(e,"occurs");
        if (e.valueOf("@occurs").equals("true")) {
            n("redefine");
        }
    }

    public void processPredicateDefinition(Element e) {
        processRedefine(e.element("Redefine"),e.element("Predicate-Pattern"));
        n("pred");
        processPredicatePattern((Element) e.selectSingleNode("./Predicate-Pattern"),true);
        processDefiniens((Element) e.selectSingleNode("./Definiens"));
    }

    public void processAttributePattern(Element e, boolean intro) {
        processLocus(e.element(ESXElementName.LOCUS));
        p("is");
        processSpellingList(e.element("Loci"));
        spelling(e);
    }

    public void processAttributeDefinition(Element e) {
        processRedefine(e.element("Redefine"),(Element) e.selectSingleNode("./Attribute-Pattern"));
        n("attr");
        processAttributePattern((Element) e.selectSingleNode("./Attribute-Pattern"),true);
        processDefiniens((Element) e.selectSingleNode("./Definiens"));
    }

    public void processAncestors(Element e) {
        if (e.selectNodes("./*").size() > 0) {
            p("(");
            processTypeList(e);
            p(")");
        }
    }

    public void processStructurePattern(Element e) {
        spelling(e);
        if (e.element("Loci").elements().size() > 0) {
            p("over");
            processSpellingList(e.element("Loci"));
        }
    }

    Element findSelector(int nr, Element selectorsList) {
        List<Element> selectorPatterns = selectorsList.elements();
        for (Element selectorPattern: selectorPatterns) {
            checkXMLAttribute(selectorPattern.element("Loci").element("Locus"),"idnr");
            if (Integer.parseInt(selectorPattern.element("Loci").element("Locus").valueOf("@idnr")) == nr) {
                return selectorPattern;
            }
        }
        return null;
    }

    public void processField(Element e, Element selectorsList) {
        List<Node> selectors = e.selectNodes("Selectors/Selector");
        int i = 0;
        for (i = 0; i < selectors.size() - 1; i++) {
            spelling(findSelector(Integer.parseInt(selectors.get(i).valueOf("@nr")),selectorsList));
            p(",");
        }
        spelling(findSelector(Integer.parseInt(selectors.get(i).valueOf("@nr")),selectorsList));
        p("->");
        processType(e.elements().get(e.elements().size() - 1));
    }

    public void processFields(Element fieldSegments, Element selectorsList) {
        List<Element> fields = fieldSegments.elements();
        p("(#");
        int i = 0;
        for (i = 0; i < fields.size() - 1; i++) {
            processField(fields.get(i),selectorsList);
            pln(",");
        }
        processField(fields.get(i),selectorsList);
        p("#)");
    }

    public void processStrict(Element e) {
    }

    public void processAggregateFunctorPattern(Element e) {
        addNameWithoutSpelling(e);
    }

    public String createSelectorName(Element e) {
        checkXMLAttribute(e,"serialnr");
        checkXMLAttribute(e,"varnr");
        return "Locus_S" + e.valueOf("@serialnr") + "_V" + e.valueOf("@varnr");
    }

    public void addSelectorName(Element e) {
    }

    public void processSelectorFunctorPattern(Element e) {
        addSelectorName((Element)e.selectSingleNode("./Loci/Locus"));
    }

    public void processSelectorsList(Element e) {
        for (Element pattern: e.elements()) {
            processSelectorFunctorPattern(pattern);
        }
    }

    public void processStructureDefinition(Element e) {
        n("struct");
        processAncestors(e.element("Ancestors"));
        processAggregateFunctorPattern(e.element("Structure-Patterns-Rendering").element("AggregateFunctor-Pattern"));
        processStructurePattern(e.element("Structure-Pattern"));
        processSelectorsList(e.element("Structure-Patterns-Rendering").element("Selectors-List"));
        processFields(e.element("Field-Segments"),e.element("Structure-Patterns-Rendering").element("Selectors-List"));
        processStrict(e.element("Structure-Patterns-Rendering").element("Strict-Pattern"));
    }

    public void processPredSynonym(Element e) {
        n("synonym");
        processPredicatePattern(e.elements().get(0),true);
        p("for");
        processPredicatePattern(e.elements().get(1).elements().get(0),false);
    }

    public void processPredAntonym(Element e) {
        n("antonym");
        processPredicatePattern(e.elements().get(0),true);
        p("for");
        processPredicatePattern(e.elements().get(1).elements().get(0),false);
    }

    public void processFuncSynonym(Element e) {
        n("synonym");
        processFunctorPattern(e.elements().get(0),true);
        p("for");
        processFunctorPattern(e.elements().get(1).elements().get(0),false);
    }

    public void processModeSynonym(Element e) {
        n("synonym");
        processModePattern(e.elements().get(0),true);
        p("for");
        processModePattern(e.elements().get(1).elements().get(0),false);
    }

    public void processAttrSynonym(Element e) {
        n("synonym");
        processAttributePattern(e.elements().get(0),true);
        p("for");
        processAttributePattern(e.elements().get(1).elements().get(0),false);
    }

    public void processAttrAntonym(Element e) {
        n("antonym");
        processAttributePattern(e.elements().get(0),true);
        p("for");
        processAttributePattern(e.elements().get(1).elements().get(0),false);
    }

    public void processEquating(Element e) {
        processVariable(e.element("Variable"));
        p("=");
        processTerm((Element) e.selectSingleNode("./*[2]"));
    }

    public void processConstantDefinition(Element e) {
        n("set");
        List<Node> equatings = e.selectNodes("./Equating");
        int i = 0;
        for (i = 0; i < equatings.size() - 1; i++) {
            processEquating((Element) equatings.get(i));
            p(",");
        }
        processEquating((Element) equatings.get(i));
    }

    public void processEquality(Element e) {
        processVariable(e.element("Variable"));
        if (e.getName().equals("Equality")) {
            p("=");
            processTerm(e.elements().get(1));
        }
    }

    public void processEqualities(Element e) {
        List<Element> equlities = e.elements();
        int i = 0;
        for (i = 0; i < equlities.size() - 1; i++) {
            processEquality(equlities.get(i));
            p(",");
        }
        processEquality(equlities.get(i));
    }

    public void processTypeChangingStatement(Element e) {
        if (isThen(e.elements().get(2)))
            n("then");
        n("reconsider");
        processEqualities(e.element("Equalities-List"));
        p("as");
        processType(e.elements().get(1));
        processJustification(e.elements().get(2));
    }

    public void processPragma(Element e) {
        switch (e.elements().get(0).getName()) {
            case "Notion-Name":
                String insc = (e.element("Notion-Name")).valueOf("@inscription");
                mainItem("::$N " + insc);
                break;
            case "Canceled":
                mainItem("::$C" + e.element("Canceled").valueOf("@kind") + " " + e.element("Canceled").valueOf("@amount"));
                break;
            case "URL":
                mainItem("::$L" + e.element("URL").valueOf("@href") + " " + e.element("URL").valueOf("@description"));
                break;
            default:
                error(e, "Pragma");
        }
    }

    public void processPrivatePredicateDefinition(Element e) {
        n("defpred");
        addPrivateName(e.element("Variable"));
        p("[");
        processTypeList(e.element("Type-List"));
        p("]");
        p("means");
        processFormula(e.elements().get(2));
    }

    public void processPrivateFunctorDefinition(Element e) {
        n("deffunc");
        addPrivateName(e.element("Variable"));
        p("(");
        processTypeList(e.element("Type-List"));
        p(")");
        p("=");
        processTerm(e.elements().get(2));
    }

    public void processModePattern(Element e, boolean intro) {
        spelling(e);
        List<Node> args = e.element("Loci").selectNodes("./*");
        if (args.size() > 0) {
            p("of");
            processSpellingList(e.element("Loci"));
        }
    }

    public void processClusteredType(Element e) {
        processAdjectiveCluster(e.elements().get(0));
        processType(e.elements().get(1));
    }

    public void processModeDefinitionKinds(Element e) {
        switch (e.getName()) {
            case "Standard-Mode":
                Element specification = (Element) e.selectSingleNode("./Type-Specification");
                if (specification != null)
                    processSpecification(specification);
                processDefiniens((Element) e.selectSingleNode("./Definiens"));
                break;
            case "Expandable-Mode":
                p("is");
                processType(e.elements().get(0));
                break;
            default:
                error(e, "MODE KIND");
        }
    }

    public void processModeDefinition(Element e) {
        processRedefine(e.element("Redefine"),(Element) e.selectSingleNode("./Mode-Pattern"));
        n("mode");
        processModePattern((Element) e.selectSingleNode("./Mode-Pattern"),true);
        processModeDefinitionKinds(e.elements().get(2));
    }

    public void processPerCases(Element e) {
        List<Node> refs = e.element("Straightforward-Justification").selectNodes("./*");
        boolean link = refs != null && refs.size() > 0 && refs.get(0).getName().equals("Link");
        n(link ? " then per cases" : " per cases");
        processStraightforwardJustification(e.element("Straightforward-Justification"));
    }

    public void processSupposeHead(Element e) {
        n("suppose");
        processAssumption(e.elements().get(0));
    }

    public void processCaseHead(Element e) {
        n("case");
        processAssumption(e.elements().get(0));
    }

    public void processReduction(Element e) {
        n("reduce");
        processTerm(e.elements().get(0));
        n("to");
        processTerm(e.elements().get(1));
    }

    public void processPropertyRegistration(Element e) {
        checkXMLAttribute(e.element("Properties"),"property");
        n(e.element("Properties").valueOf("@property"));
        p("of");
        processType((Element) e.selectSingleNode("./Properties/*[1]"));
        processJustification(e.elements().get(1));
    }

    public void processLociEquality(Element e) {
        spelling(e.elements().get(0));
        p("=");
        spelling(e.elements().get(1));
    }

    public void processLociEqualities(Element e) {
        List<Element> equalities = e.elements();
        if (equalities.size() > 0) {
            p("when");
            int i = 0;
            for (i = 0; i < equalities.size() - 1; i++) {
                processLociEquality(equalities.get(i));
                p(",");
            }
            processLociEquality(equalities.get(i));
        }
    }

    public void processIdentify(Element e) {
        n("identify");
        processFunctorPattern(e.elements().get(1).elements().get(0),false);
        p("with");
        processFunctorPattern(e.elements().get(0).elements().get(0),false);
        processLociEqualities(e.element("Loci-Equalities"));
    }

    public void processItem(Element e) {
        switch (e.getName()) {
            case "Section-Pragma":
                processSectionItem(e);
                break;
            case "Reservation":
                processReservationItem(e);
                break;
            case "Regular-Statement":
                processRegularStatement(e);
                break;
            case "Theorem-Item":
                processTheoremItem(e);
                break;
            case "Definition-Item":
                processDefinitionItem(e);
                break;
            case "Cluster":
                processCluster(e.elements().get(0));
                break;
            case "Mode-Definition":
                processModeDefinition(e);
                break;
            case "Scheme-Block-Item":
                processSchemeBlockItem(e);
                break;
            case "Scheme-Head":
                processSchemeHead(e);
                break;
            case "Loci-Declaration":
                processLociDeclaration(e);
                break;
            case "Functor-Definition":
                processFunctorDefinition(e);
                break;
            case "Predicate-Definition":
                processPredicateDefinition(e);
                break;
            case "Pred-Synonym":
                processPredSynonym(e);
                break;
            case "Pred-Antonym":
                processPredAntonym(e);
                break;
            case "Attr-Synonym":
                processAttrSynonym(e);
                break;
            case "Attr-Antonym":
                processAttrAntonym(e);
                break;
            case "Func-Synonym":
                processFuncSynonym(e);
                break;
            case "Mode-Synonym":
                processModeSynonym(e);
                break;
            case "Attribute-Definition":
                processAttributeDefinition(e);
                break;
            case "Structure-Definition":
                processStructureDefinition(e);
                break;
            case "Correctness-Condition":
                processCorrectnessCondition(e);
                break;
            case "Correctness":
                processCorrectness(e);
                break;
            case "Pragma":
                processPragma(e);
                break;
            case "Choice-Statement":
                processChoiceStatement(e);
                break;
            case "Exemplification":
                processExemplification(e);
                break;
            case "Conclusion":
                processConclusion(e);
                break;
            case "Generalization":
                processGeneralization(e);
                break;
            case "Default-Generalization":
                break;
            case "Assumption":
                n("assume");
                processAssumption(e.elements().get(0));
                break;
            case "Existential-Assumption":
                processExistentialAssumption(e);
                break;
            case "Constant-Definition":
                processConstantDefinition(e);
                break;
            case "Type-Changing-Statement":
                processTypeChangingStatement(e);
                break;
            case "Property":
                processProperty(e);
                break;
            case "Private-Predicate-Definition":
                processPrivatePredicateDefinition(e);
                break;
            case "Private-Functor-Definition":
                processPrivateFunctorDefinition(e);
                break;
            case "Per-Cases":
                processPerCases(e);
                break;
            case "Case-Block":
                break;
            case "Suppose-Head":
                processSupposeHead(e);
                break;
            case "Case-Head":
                processCaseHead(e);
                break;
            case "Reduction":
                processReduction(e);
                break;
            case "Property-Registration":
                processPropertyRegistration(e);
                break;
            case "Identify":
                processIdentify(e);
                break;
            default:
                error(e, "ITEM");
        }
    }

    public void processReservationItem(Element e) {
        mainItem("reserve");
        List<Element> vars = e.elements("Reservation-Segment");
        int i = 0;
        for (i = 0; i < vars.size() - 1; i++) {
            processReservationSegment(vars.get(i), ",");
            n("");
        }
        processReservationSegment(vars.get(i), "");
    }

    public void processReservationSegment(Element e, String separator) {
        processVariables(e);
        p("for");
        processType((Element) e.selectSingleNode("./*[3]"));
        p(separator);
    }

    public void processVariable(Element e) {
        spelling(e);
    }

    public void processVariables(Element e) {
        List<Element> vars = e.element(ESXElementName.VARIABLES).elements();
        if (vars.size() == 0)
            return;
        int i = 0;
        for (i = 0; i < vars.size() - 1; i++) {
            processVariable(vars.get(i));
            p(",");
        }
        processVariable(vars.get(i));
    }

    public void preProcess(Element e) {
        switch (e.getName()) {
            case "Text-Proper":
                processTextProper();
                break;
            case "Item":
                textLevel++;
                processItem((Element) e.selectSingleNode("./*[1]"));
                break;
            case "Test":
                if (e.getName().equals("Test")) {
                    List<Node> n = e.selectNodes(".//d[@kind='1']");
                    System.out.print(n.size() + " ");
                }
                break;
            default:
//			p("UNKNOWN ELEMENT " + e.getName() + "\n");
        }
    }

    public void processItemEnd(Element e) {
        switch (e.attributeValue("kind")) {
            case "Section-Pragma":
            case "Definition-Item":
            case "Default-Generalization":
            case "Generalization":
            case "Pragma":
                break;
            default:
                p(";");
        }
    }

    public void postProcess(Element e) {
        switch (e.getName()) {
            case "Text-Proper":
                break;
            case "Item":
                processItemEnd(e);
                textLevel--;
                break;
            case "Block":
                if (e.getParent().getName().equals("Definition-Item")) {
                    n("end;");
                } else {
                    n("end");
                }
                break;
        }
    }

    public void treeWalk(Document document) {
        preProcess(document.getRootElement());
        treeWalk(document.getRootElement());
        postProcess(document.getRootElement());
    }

    public void treeWalk(Element element) {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Element) {
                preProcess((Element) node);
                treeWalk((Element) node);
                postProcess((Element) node);
            } else {
            }
        }
    }

    public void writeNewMIZ() {
        try {
            FileWriter fw = new FileWriter(outFile);
            fw.write(outString + "\n");
            System.out.println(newFileName + " file created.");
            fw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeErrors() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter out = null;
        try {
//            fw = new FileWriter(this.filesPath + "/" + this.fileName + ".esxERRORS", true);
            fw = new FileWriter("miz_errors.txt", true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            out.println("#" + getFileName());
            for (String error : errors)
                out.println("  ERROR: " + error);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processArticle() {
        try {
            treeWalk(getDocument());
            writeNewMIZ();
        } catch (Exception e) {
            errors.add("RUNTIME " + e.getClass().getName() + ": " + e.toString());
        }
//        System.out.println("\n" + getFileName() + ": " + errorNbr + " error(s) found.");
//        System.out.println(errors);
//        writeErrors();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Enter file name");
            return;
        }
        ESX_2_MIZ app = new ESX_2_MIZ(args[0], ".new");
        app.processArticle();
    }
}
