package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Result;
import utilities.ServerConfigurations;
import utilities.file_system.IVF;
import utilities.file_system.commons_vfs2.CommonsVF;
import utilities.outline.OutlineTreeContentProvider;

import java.util.List;

public class outline extends Application {
    public Result file(String account, String absPath) {
        IVF file = new CommonsVF(ServerConfigurations.basePath + "/" + account + "/" + absPath);

        if (!file.exists())
            return ok();

        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(file);
        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list, file.getName());

        return ok(jsonList.toString());
    }
}
