package core.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.runtime.RuntimeManager;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Release;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import org.overture.webide.processor.ProcessingResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModelWrapper {
    private ModuleInterpreter interpreter;

    private List<File> files;
    private Release release;

    public List<VDMWarning> parserWarnings;
    public List<VDMWarning> typeCheckerWarnings;
    public List<VDMError> parserErrors;
    public List<VDMError> typeCheckerErrors;

    public ModelWrapper(IVFS<FileObject> file) {
        this.files = preprocessFiles(file);
        //this.release = getRelease(file);
    }

    public String evaluate(String input) {
        Evaluator evaluator = new Evaluator(this.interpreter);
        return evaluator.evaluate(input);
    }

    public String getTargetModuleName() {
        return this.interpreter.getDefaultName();
    }

    public ModuleList getAst() {
        if (this.interpreter != null)
            return this.interpreter.getModules();
        else
            return new ModuleList();
    }

    public IProofObligationList getPog() throws AnalysisException {
        if (this.interpreter != null && this.interpreter.defaultModule.getTypeChecked())
            return this.interpreter.getProofObligations();
        return new ProofObligationList();
    }

    private List<File> preprocessFiles(IVFS<FileObject> file) {
        List<File> files = file.getProjectAsIOFile();
        List<File> filteredFiles = Collections.synchronizedList(new ArrayList<>());
        filteredFiles.addAll(files.stream().filter(f -> f.getName().endsWith(".vdmsl")).collect(Collectors.toList()));
        return filteredFiles;
    }

    public ModelWrapper init() {
        ModuleList ast;
        List<AModuleModules> result = null;

        ProcessingResult res = new RuntimeManager().process(this.files);

        if (res != null) {
            this.parserWarnings = res.getParserWarnings();
            this.parserErrors = res.getParserErrors();
            this.typeCheckerWarnings = res.getTypeCheckerWarnings();
            this.typeCheckerErrors = res.getTypeCheckerErrors();
            result = res.modules;
        } else {
            this.parserWarnings = new ArrayList<>();
            this.parserErrors = new ArrayList<>();
            this.typeCheckerWarnings = new ArrayList<>();
            this.typeCheckerErrors = new ArrayList<>();
        }

        if (result == null) {
            ast = new ModuleList();
        } else {
            ast = new ModuleList(result);
            ast.combineDefaults();
        }

        try {
            this.interpreter = new ModuleInterpreter(ast);
            this.interpreter.defaultModule.setTypeChecked(this.parserErrors.isEmpty() && this.typeCheckerErrors.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private Release getRelease(IVFS<FileObject> file) {
        try {
            String attribute = "release";
            FileObject projectRoot = file.getProjectRoot();
            if (projectRoot == null)
                return Release.DEFAULT;

            FileObject projectFile = projectRoot.getChild(".project");
            if (projectFile == null)
                return Release.DEFAULT;

            InputStream content = projectFile.getContent().getInputStream();
            JsonNode node = new ObjectMapper().readTree(content);
            Release release = null;
            if (node != null && node.hasNonNull(attribute))
                release = Release.lookup(node.get(attribute).textValue());

            return release != null ? release : Release.DEFAULT;
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return Release.DEFAULT;
    }
}
