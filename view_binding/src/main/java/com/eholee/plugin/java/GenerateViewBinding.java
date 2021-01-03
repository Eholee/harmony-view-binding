package com.eholee.plugin.java;

import com.squareup.javapoet.*;
import org.gradle.api.Project;
import org.gradle.api.logging.Logging;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateViewBinding {
    private static final String JOINT_MARK1 = ".JM1.";
    private static final String JOINT_MARK2 = ".JM2.";
    private static final String PACKAGE_NAME_SUFFIX = "com.eholee.viewbinding.";
    private static Map<String, List<String>> componentIds = new HashMap<>();

    public static void execute(String packageName , Project project, boolean enable) {
        if (enable) {

            // step1  遍历src/main/resources/base/layout 目录下xml文件名并解析出所有组件ID

            String layoutDir = project.getProjectDir().getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "base" + File.separator + "layout";
            File layoutDirFile = new File(layoutDir);
            if (layoutDirFile.exists()) {
                if (layoutDirFile.listFiles().length > 0) {
                    for (File file : layoutDirFile.listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".xml")) {
                            // 是文件且是xml文件
                            String fileName = file.getName().replace(".xml", "");
                            String bindingClassName;
                            if (fileName.contains("_")) {
                                // 按下划线切割
                                StringBuilder tempClassName = new StringBuilder();
                                for (String s : fileName.split("_")) {
                                    tempClassName.append(s.substring(0, 1).toUpperCase().concat(s.substring(1)));
                                }
                                bindingClassName = tempClassName.append("Binding").toString();
                            } else {
                                // 不包含 首字母大写
                                bindingClassName = fileName.substring(0, 1).toUpperCase().concat(fileName.substring(1)).concat("Binding");
                            }
                            String filePath = file.getAbsolutePath();

                            String rootEleName = XmlUtil.getRootElement(filePath).getName();
                            boolean isRootElementEnableViewBinding = XmlUtil.getRootElementViewBindingEnable(filePath);
                            if (!isRootElementEnableViewBinding){
                                Logging.getLogger(CommonUtil.class).error(String.format("%s`s layout rootElement-%s contains view_binding attribute equals false " , bindingClassName , rootEleName));
                                continue;
                            }
                            List<String> ids = new ArrayList<>();
                            XmlUtil.getAllIdElement(XmlUtil.getRootElement(filePath), ids);

                            componentIds.put(bindingClassName.concat(JOINT_MARK1).concat(rootEleName).concat(JOINT_MARK2).concat(fileName), ids);

                        }
                    }

                    if (componentIds != null && componentIds.size() > 0) {
                        // 存在组件ID
                        //  已收集到所有带ID的控件
                        for (String classAndRootAndXmlName : componentIds.keySet()) {
                            String xmlFileName = classAndRootAndXmlName.substring(classAndRootAndXmlName.lastIndexOf(JOINT_MARK2) + JOINT_MARK2.length());
                            String className = classAndRootAndXmlName.substring(0, classAndRootAndXmlName.lastIndexOf(JOINT_MARK1));
                            String rootElementName = classAndRootAndXmlName.substring(classAndRootAndXmlName.lastIndexOf(JOINT_MARK1) + JOINT_MARK1.length(), classAndRootAndXmlName.lastIndexOf(JOINT_MARK2));
                            List<String> componentsAndIds = componentIds.get(className.concat(JOINT_MARK1).concat(rootElementName).concat(JOINT_MARK2).concat(xmlFileName));

                            //step2 : 创建类
                            Logging.getLogger(CommonUtil.class).error(String.format("prepare to generate viewBinding class: %s ...", className));

                            generateViewBindingClass(packageName ,project, className, rootElementName, componentsAndIds, xmlFileName);
                        }
                    } else {
                        //  不存在组件ID  不处理
                    }

                } else {
                    // 没有xml文件 不处理
                }
            } else {
                // 不存在layout文件夹 不处理
                System.out.println("layoutDir not exsit");

            }
        } else {
            // +File.separator+"com"+File.separator+"eholee"+File.separator+"lobster"+File.separator+"main_app"+File.separator+"Lobster.java"
            // 关闭该选项  检测上一次是否生成了文件，生成了需要删除
            File buildFile = new File(project.getBuildDir().getAbsolutePath() + File.separator + "generated" + File.separator + "source" + File.separator + "viewBinding");
            if (buildFile.exists()) {
                FileUtil.deleteAllFile(buildFile.getAbsolutePath());
                buildFile.delete();
            } else {

            }
        }

    }

    /**
     * 创建ViewBinding类
     *
     * @param project         项目
     * @param className       类名
     * @param rootElementName 根组件名称
     * @param componentsAndIds    组件及其id集合
     * @param xmlFileName     xml文件名
     */
    private static void generateViewBindingClass(String packageName ,Project project, String className, String rootElementName, List<String> componentsAndIds, String xmlFileName) {

        TypeSpec.Builder viewBindingClassBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        viewBindingClassBuilder.addFields(getFieldSpecList(rootElementName , componentsAndIds));
        // 创建构造方法
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameters(getConstructorParameters(rootElementName , componentsAndIds))
                .addStatement(getConstructorStatements(rootElementName , componentsAndIds))
                .build();
        viewBindingClassBuilder.addMethod(constructor);
        // 创建getRoot方法
       /* public DependentLayout getRoot() {
            return rootView;
        }*/
        MethodSpec getRoot = MethodSpec.methodBuilder("getRoot")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("ohos.agp.components" ,rootElementName))
                .addStatement("return rootView")
                .build();
        viewBindingClassBuilder.addMethod(getRoot);

        // 创建布局填充方法
        ParameterSpec parseMethodParameter = ParameterSpec.builder(ClassName.get("ohos.app" ,"Context"),"context")
                .build();
        MethodSpec parse = MethodSpec.methodBuilder("parse")
                .addModifiers(Modifier.PUBLIC , Modifier.STATIC)
                .addParameter(parseMethodParameter)
                .returns(ClassName.get(PACKAGE_NAME_SUFFIX.concat(project.getName()) ,className))
                .addStatement("return parse(context , null , false)")
                .build();
        viewBindingClassBuilder.addMethod(parse);

        ParameterSpec parseMethod2Parameter1 = ParameterSpec.builder(ClassName.get("ohos.app" ,"Context"),"context")
                .build();
        ParameterSpec parseMethod2Parameter2 = ParameterSpec.builder(ClassName.get("ohos.agp.components" ,"ComponentContainer"),"parent")
                .build();
        ParameterSpec parseMethod2Parameter3 = ParameterSpec.builder(boolean.class,"attachToRoot")
                .build();
        MethodSpec parse2 = MethodSpec.methodBuilder("parse")
                .addModifiers(Modifier.PUBLIC , Modifier.STATIC)
                .addParameter(parseMethod2Parameter1)
                .addParameter(parseMethod2Parameter2)
                .addParameter(parseMethod2Parameter3)
                .returns(ClassName.get(PACKAGE_NAME_SUFFIX.concat(project.getName()) ,className))
                .addStatement("$T root = $T.getInstance("+parseMethod2Parameter1.name+").parse($T.Layout_"+xmlFileName+","+parseMethod2Parameter2.name+",false)" ,
                        ClassName.get("ohos.agp.components" , "Component"),
                        ClassName.get("ohos.agp.components" , "LayoutScatter"),
                        ClassName.get(packageName , "ResourceTable")
                )
                .beginControlFlow("if("+parseMethod2Parameter3.name+")")
                .addStatement(parseMethod2Parameter2.name+".addComponent(root)")
                .endControlFlow()
                .addStatement("return bind(root)")
                .build();
        viewBindingClassBuilder.addMethod(parse2);

        // 创建bind方法
        ParameterSpec bindParameter = ParameterSpec.builder(ClassName.get("ohos.agp.components" ,"Component"),"rootView")
                .build();
        MethodSpec bind = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC , Modifier.STATIC)
                .addParameter(bindParameter)
                .returns(ClassName.get(PACKAGE_NAME_SUFFIX.concat(project.getName()) ,className))
                .addStatement("$T missingId" , String.class)
                .beginControlFlow("missingId:")
                .addStatement(getBindMethodStatements(packageName ,className ,rootElementName ,componentsAndIds))
                .endControlFlow()
                .addStatement("throw new $T($S"+"+missingId)" , NullPointerException.class , "Missing required view with ID:")
                .build();
        viewBindingClassBuilder.addMethod(bind);

        TypeSpec viewBindingClass = viewBindingClassBuilder.build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME_SUFFIX.concat(project.getName()), viewBindingClass)
                .build();
        File buildFile = new File(project.getBuildDir().getAbsolutePath() + File.separator + "generated" + File.separator + "source" + File.separator + "viewBinding");
        try {
            javaFile.writeTo(buildFile);
            Logging.getLogger(CommonUtil.class).error("generated successfully");

        } catch (IOException e) {
            Logging.getLogger(CommonUtil.class).error("generated unsuccessfully");

            e.printStackTrace();
        }
    }


    private static List<FieldSpec> getFieldSpecList(String rootComponentName , List<String> componentsAndIds){
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        FieldSpec rootFieldSpec = FieldSpec.builder(ClassName.get("ohos.agp.components" ,rootComponentName) , "rootView")
                .addModifiers(Modifier.PUBLIC , Modifier.FINAL)
                .build();
        fieldSpecList.add(rootFieldSpec);
        for (String componentNameAndId : componentsAndIds) {
            String componentId = componentNameAndId.substring(0, componentNameAndId.lastIndexOf("_"));
            String componentName = componentNameAndId.substring(componentNameAndId.lastIndexOf("_") + 1);
            String finalComponentName;
            if (componentId.contains("_")) {
                // 按下划线切割
                StringBuilder tempComponentName = new StringBuilder();
                String[] componentLowercaseArray = componentId.toLowerCase().split("_");
                for (int i = 0; i < componentLowercaseArray.length; i++) {
                    if (i==0){
                        tempComponentName.append(componentLowercaseArray[0]);
                    }else{
                        tempComponentName.append(componentLowercaseArray[i].substring(0, 1).toUpperCase().concat(componentLowercaseArray[i].substring(1)));
                    }
                }
                finalComponentName = tempComponentName.toString();
            } else {
                // 不包含 首字母大写
                finalComponentName = componentId.substring(0, 1).toLowerCase().concat(componentId.substring(1));
            }

            FieldSpec componentField = FieldSpec.builder(ClassName.get("ohos.agp.components" ,componentName) , finalComponentName)
                    .addModifiers(Modifier.PUBLIC , Modifier.FINAL)
                    .build();
            fieldSpecList.add(componentField);


        }
        return fieldSpecList;
    }


    private static List<ParameterSpec> getConstructorParameters(String rootComponentName , List<String> componentsAndIds){
        List<ParameterSpec> parameterSpecList = new ArrayList<>();
        ParameterSpec rootParameterSpec = ParameterSpec.builder(ClassName.get("ohos.agp.components" ,rootComponentName) , "rootView")
                .build();
        parameterSpecList.add(rootParameterSpec);
        for (String componentNameAndId : componentsAndIds) {
            String componentId = componentNameAndId.substring(0, componentNameAndId.lastIndexOf("_"));
            String componentName = componentNameAndId.substring(componentNameAndId.lastIndexOf("_") + 1);
            String finalComponentName;
            if (componentId.contains("_")) {
                // 按下划线切割
                StringBuilder tempComponentName = new StringBuilder();
                String[] componentLowercaseArray = componentId.toLowerCase().split("_");
                for (int i = 0; i < componentLowercaseArray.length; i++) {
                    if (i==0){
                        tempComponentName.append(componentLowercaseArray[0]);
                    }else{
                        tempComponentName.append(componentLowercaseArray[i].substring(0, 1).toUpperCase().concat(componentLowercaseArray[i].substring(1)));
                    }
                }
                finalComponentName = tempComponentName.toString();
            } else {
                // 不包含 首字母大写
                finalComponentName = componentId.substring(0, 1).toLowerCase().concat(componentId.substring(1));
            }
            ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get("ohos.agp.components" ,componentName) , finalComponentName)
                    .build();
            parameterSpecList.add(parameterSpec);
        }
        return parameterSpecList;
    }

    private static CodeBlock getConstructorStatements(String rootComponentName , List<String> componentsAndIds){
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        codeBlockBuilder.add("this.rootView = rootView;\n");
        for (String componentNameAndId : componentsAndIds) {
            String componentId = componentNameAndId.substring(0, componentNameAndId.lastIndexOf("_"));
            String componentName = componentNameAndId.substring(componentNameAndId.lastIndexOf("_") + 1);
            String finalComponentName;
            if (componentId.contains("_")) {
                // 按下划线切割
                StringBuilder tempComponentName = new StringBuilder();
                String[] componentLowercaseArray = componentId.toLowerCase().split("_");
                for (int i = 0; i < componentLowercaseArray.length; i++) {
                    if (i==0){
                        tempComponentName.append(componentLowercaseArray[0]);
                    }else{
                        tempComponentName.append(componentLowercaseArray[i].substring(0, 1).toUpperCase().concat(componentLowercaseArray[i].substring(1)));
                    }
                }
                finalComponentName = tempComponentName.toString();
            } else {
                // 不包含 首字母大写
                finalComponentName = componentId.substring(0, 1).toLowerCase().concat(componentId.substring(1));
            }
            if (componentsAndIds.indexOf(componentNameAndId) == componentsAndIds.size()-1){
                // 最后一个不追加分号
                codeBlockBuilder.add("this."+finalComponentName+" = "+ finalComponentName);

            }else{
                codeBlockBuilder.add("this."+finalComponentName+" = "+ finalComponentName+";\n");

            }

        }
        return codeBlockBuilder.build();
    }


    private static CodeBlock getBindMethodStatements(String packageName ,String className ,String rootComponentName , List<String> componentsAndIds){
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        CodeBlock.Builder codeBlockBuilder2 = CodeBlock.builder();
        codeBlockBuilder2.add("return new "+className+"(("+rootComponentName+") rootView");
        for (String componentNameAndId : componentsAndIds) {
            String componentId = componentNameAndId.substring(0, componentNameAndId.lastIndexOf("_"));
            String componentName = componentNameAndId.substring(componentNameAndId.lastIndexOf("_") + 1);
            String finalComponentName;
            if (componentId.contains("_")) {
                // 按下划线切割
                StringBuilder tempComponentName = new StringBuilder();
                String[] componentLowercaseArray = componentId.toLowerCase().split("_");
                for (int i = 0; i < componentLowercaseArray.length; i++) {
                    if (i==0){
                        tempComponentName.append(componentLowercaseArray[0]);
                    }else{
                        tempComponentName.append(componentLowercaseArray[i].substring(0, 1).toUpperCase().concat(componentLowercaseArray[i].substring(1)));
                    }
                }
                finalComponentName = tempComponentName.toString();
            } else {
                // 不包含 首字母大写
                finalComponentName = componentId.substring(0, 1).toLowerCase().concat(componentId.substring(1));
            }
            codeBlockBuilder.add(componentName+" "+finalComponentName+"="+"("+componentName+")"+"rootView.findComponentById($T.Id_"+componentId+");\n",ClassName.get(packageName , "ResourceTable"));
            codeBlockBuilder.add("if("+finalComponentName+" == null){\n\tmissingId = $S;\n\tbreak missingId;\n}" , finalComponentName);

        }

        for (String componentNameAndId : componentsAndIds) {
            String componentId = componentNameAndId.substring(0, componentNameAndId.lastIndexOf("_"));
            String componentName = componentNameAndId.substring(componentNameAndId.lastIndexOf("_") + 1);
            String finalComponentName;

            if (componentId.contains("_")) {
                // 按下划线切割
                StringBuilder tempComponentName = new StringBuilder();
                String[] componentLowercaseArray = componentId.toLowerCase().split("_");
                for (int i = 0; i < componentLowercaseArray.length; i++) {
                    if (i==0){
                        tempComponentName.append(componentLowercaseArray[0]);
                    }else{
                        tempComponentName.append(componentLowercaseArray[i].substring(0, 1).toUpperCase().concat(componentLowercaseArray[i].substring(1)));
                    }
                }
                finalComponentName = tempComponentName.toString();
            } else {
                // 不包含 首字母大写
                finalComponentName = componentId.substring(0, 1).toLowerCase().concat(componentId.substring(1));
            }
            codeBlockBuilder2.add(","+finalComponentName);

        }
        codeBlockBuilder2.add(")");

        codeBlockBuilder.add(codeBlockBuilder2.build());
        return codeBlockBuilder.build();
    }
}
