package com.jack;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

/**
 * by shenmingliang1
 * 2019.01.24 17:27.
 */
public class Generate extends AnAction {
    private PsiClass mClass;
    private Editor mEditor;
    private Project mProject;
    private Document mDocument;
    private CaretModel mCaret;
    private PsiFile mFile;

    private String JSON_PACKAGE_IMPORT;
    private String LIBRARY_IMPORT;
    private String PART_IMPORT;
    private String ANNOTATION;
    private String JSON_METHOD;
    private String mClassName = "";

    @Override
    public void actionPerformed(AnActionEvent e) {
        initParams(e);
        SelectionModel selectionModel = mEditor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            mClassName = selectionModel.getSelectedText().trim();
            if (mClassName.equals("")) {
                return;
            }

            JSON_PACKAGE_IMPORT = "import 'package:json_annotation/json_annotation.dart';\n\n";
            LIBRARY_IMPORT = "library " + mClassName.toLowerCase() + ";\n\n";
            PART_IMPORT = "part '" + mClassName + ".g.dart';\n\n";
            ANNOTATION = "@JsonSerializable()\n";
            JSON_METHOD = "    factory " + mClassName
                    + ".fromJson(Map<String, dynamic> json) => _$" + mClassName
                    + "FromJson(json);\n\n"
                    + "     Map<String, dynamic> toJson("
                    + mClassName
                    + " instance) => _$"
                    + mClassName
                    + "ToJson(instance);\n\n";

            int line = mCaret.getVisualPosition().line;
            int column = mCaret.getVisualPosition().column;
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                mDocument.insertString(line - 1 == -1 ? 0 : line - 1, ANNOTATION);
                mDocument.insertString(0, LIBRARY_IMPORT + JSON_PACKAGE_IMPORT
                        + PART_IMPORT);
            });

            mCaret.moveToVisualPosition(new VisualPosition(line + 8, 0));
            int offset = mCaret.getOffset();
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                mDocument.insertString(offset == -1 ? 0 : offset, JSON_METHOD);
            });

            mCaret.moveToVisualPosition(new VisualPosition(line + 7, column));
            selectionModel.selectWordAtCaret(true);
        }
    }

    private void initParams(AnActionEvent e) {
        mEditor = e.getRequiredData(CommonDataKeys.EDITOR);
        mProject = e.getProject();
        mDocument = mEditor.getDocument();
        mCaret = mEditor.getCaretModel();
        mFile = PsiUtilBase.getPsiFileInEditor(mEditor, mProject);
//        mClass = getTargetClass(mEditor, mFile);
//        mClassName = mClass.getName();
    }

    private PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    private void showInfoDialog(String message) {
        Messages.showErrorDialog(message, "Info");
    }
}