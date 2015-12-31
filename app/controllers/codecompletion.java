package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.codecompletion.assistant.ContentAssistProcessor;
import core.codecompletion.proposal.mapper.ProposalToJsonMapper;
import core.codecompletion.resources.Document;
import core.codecompletion.resources.ICompletionProposal;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.overture.interpreter.util.ExitStatus;
import play.mvc.Result;

import java.util.List;

public class codecompletion extends Application {
    public Result proposal(String account, String path) {
        String lineStr = request().getQueryString("line");
        String columnStr = request().getQueryString("column");
        int line;
        int column;

        if (lineStr == null || columnStr == null)
            return status(StatusCode.UnprocessableEntity, "Missing query argument");

        try {
            line = Integer.parseInt(lineStr);
            column = Integer.parseInt(columnStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return status(StatusCode.UnprocessableEntity, "Invalid query argument format");
        }

        IVFS file = new CommonsVFS(PathHelper.JoinPath(account, path));

        Document document = new Document(file.getIOFile());

        if (ExitStatus.EXIT_ERRORS == document.parse())
            return status(StatusCode.UnprocessableEntity, "Errors on parse");

        if (ExitStatus.EXIT_ERRORS == document.typeCheck())
            return status(StatusCode.UnprocessableEntity, "Errors on typeCheck");

        ContentAssistProcessor cap = new ContentAssistProcessor();
        List<ICompletionProposal> proposals = cap.computeCompletionProposals(document, line, column);

        ProposalToJsonMapper mapper = new ProposalToJsonMapper();
        List<ObjectNode> proposalsAsJson = mapper.toJson(proposals, document);

        return ok(proposalsAsJson.toString());
    }
}
