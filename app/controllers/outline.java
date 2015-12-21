package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Result;
import utilities.ServerConfigurations;
import utilities.file_system.ICustomVF;
import utilities.file_system.commons_vfs2.CommonsVF;
import utilities.outline.OutlineTreeContentProvider;

import java.util.List;

public class outline extends Application {
    public Result index(String account, String absPath) {

        ICustomVF file = new CommonsVF(ServerConfigurations.basePath + "/" + account + "/" + absPath);

        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(file);

        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list);

        return ok(jsonList.toString());
    }
}