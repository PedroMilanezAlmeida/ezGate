package com.flowjo.plugin.ezDAFi;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.treestar.lib.file.FileUtil;
import com.treestar.flowjo.engine.EngineManager;
import com.treestar.flowjo.engine.utility.RFlowCalculator;

import com.flowjo.plugin.ezDAFi.utils.FilenameUtils;

public class ezDAFiRFlowCalc extends RFlowCalculator {

    // The path to the ezDAFi R script template from within the jar file
    private final static String ezDAFiTemplatePath = "r/RScript.ezDAFi.Template.R";
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";


    public File runezDAFi(String thisSampleURI, String wsName, String wsDir, File sampleFile, String sampleName, String populationName, String sampleNodeName, List<String> parameterNames, Map<String, String> options, String outputFolderPath, boolean useExistingFiles, long millisTime)
    {
        sampleName = sampleName.replaceAll(".ExtNode", "").replaceAll(".fcs", "").replaceAll(".LMD", "").trim();
        File outputFolder = new File(outputFolderPath);

        StringWriter scriptWriter = new StringWriter();
        File ezDAFiScript = createezDAFiscript(thisSampleURI, wsName, wsDir, sampleFile, sampleName, populationName, sampleNodeName, parameterNames, options, outputFolder, scriptWriter, millisTime);
        if(ezDAFiScript == null) return null;
        if(useExistingFiles && ezDAFiScript.exists()) return ezDAFiScript;

        String scriptFileName = (new StringBuilder()).append("RScript.ezDAFi.").append(System.currentTimeMillis()).append(".R").toString().replaceAll(" ", "_");
        try
        {
            com.flowjo.plugin.ezDAFi.RScriptFlowCalculator calc = new com.flowjo.plugin.ezDAFi.RScriptFlowCalculator();
            fScriptFile = new File(outputFolderPath, scriptFileName);
            FileUtil.write(fScriptFile, scriptWriter.toString());
            calc.setRScriptMode(false);
            calc.executeRBatch(fScriptFile);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return ezDAFiScript;
    }

    protected File createezDAFiscript(String thisSampleURI, String wsName, String wsDir, File sampleFile, String sampleName, String populationName, String sampleNodeName, List<String> parameterNames, Map<String, String> options, File outputFolder, StringWriter scriptWriter, long millisTime)
    {
        InputStream scriptStream = ezDAFiRFlowCalc.class.getResourceAsStream(ezDAFiTemplatePath);

        String outFileName = (new StringBuilder()).append(FilenameUtils.fixFileNamePart(sampleName)).append(".ezDAFi").append(".csv").toString();

        if(outputFolder == null) {
            String outputFolderMillisTime = sampleFile.getParentFile().getAbsolutePath() + File.separator + millisTime;
            outputFolder = new File(outputFolderMillisTime);
        }
        String outputFolderString = outputFolder.getAbsolutePath();

        File outFile = new File(outputFolder, outFileName);
        outFileName = outFile.getAbsolutePath();
        File gatingMLOutFile = new File(outFile, ".gating-ml2.xml");

        String dataFilePath = sampleFile.getAbsolutePath();

        String millisTimeString = Long.toString(millisTime);
        System.out.println("millisTimeString:" + millisTimeString);

        if(EngineManager.isWindows()) outFileName = outFileName.replaceAll("\\\\", "/");
        if(EngineManager.isWindows()) dataFilePath = dataFilePath.replaceAll("\\\\", "/");
        if(EngineManager.isWindows()) outputFolderString = outputFolderString.replaceAll("\\\\", "/");

        //String sParScale = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.scaleOptionName);
        String sParPlotStats = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.plotStatsOptionName);
        //String sParTrans = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.transOptionName);
        String sParBatch = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.batchOptionName);
        String sParkMeansSom = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.kMeansSomOptionName);
        String sParPLS = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.PLSOptionName);
        String sParMeta = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.metaOptionName);
        String sParApplyOnChildren = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.applyOnChildrenOptionName);
        String sParMinPopSize = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.minPopSizeOptionName);
        String sParMinDim = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.minDimOptionName);
        String sParMaxDim = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.maxDimOptionName);
        String sParXDim = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.xDimOptionName);
        String sParYDim = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.yDimOptionName);
        String sParApplyOnPrev = options.get(com.flowjo.plugin.ezDAFi.ezDAFi.applyOnPrevOptionName);
        String sAddCellIdToResults = com.flowjo.plugin.ezDAFi.ezDAFi.isSeqGeq() ? TRUE : FALSE;
        //Added this to add runID to parameter
        String parameterName = com.flowjo.plugin.ezDAFi.ezDAFi.pluginName;

        //if (sParScale == null || sParScale.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParScale) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParScale))
//            sParScale = TRUE; // TRUE is the default
        //      else
        //sParScale = FALSE;

        if (sParPlotStats == null || sParPlotStats.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParPlotStats) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParPlotStats))
            sParPlotStats = TRUE; // TRUE is the default
              else
        sParPlotStats = FALSE;

        //if (sParTrans == null || sParTrans.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParTrans) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParTrans))
        //  sParTrans = TRUE; // TRUE is the default
        //else
        //  sParTrans = FALSE;

        if (sParBatch == null || sParBatch.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParBatch) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParBatch))
            sParBatch = TRUE; // TRUE is the default
        else
            sParBatch = FALSE;

        if (sParkMeansSom == null || sParkMeansSom.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParkMeansSom) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParkMeansSom))
            sParkMeansSom = TRUE; // TRUE is the default
        else
            sParkMeansSom = FALSE;

        if (sParPLS == null || sParPLS.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParPLS) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParPLS))
            sParPLS = TRUE; // TRUE is the default
        else
            sParPLS = FALSE;

        if (sParMeta == null || sParMeta.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParMeta) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParMeta))
            sParMeta = TRUE; // TRUE is the default
        else
            sParMeta = FALSE;

        if (sParApplyOnChildren == null || sParApplyOnChildren.isEmpty() || com.flowjo.plugin.ezDAFi.ezDAFi.One.equals(sParApplyOnChildren) || com.flowjo.plugin.ezDAFi.ezDAFi.True.equals(sParApplyOnChildren))
            sParApplyOnChildren = TRUE; // TRUE is the default
        else
            sParApplyOnChildren = FALSE;

        if (sParApplyOnPrev == null || sParApplyOnPrev.isEmpty())
            sParApplyOnPrev = com.flowjo.plugin.ezDAFi.ezDAFi.defaultApplyOnPrev;
        if (sParApplyOnPrev.length() > 5) // i.e., not "None"
            sParApplyOnPrev = outputFolder + File.separator + sParApplyOnPrev;
        if(EngineManager.isWindows()) sParApplyOnPrev = sParApplyOnPrev.replaceAll("\\\\", "/");

        if(EngineManager.isWindows()) wsDir = wsDir.replaceAll("\\\\", "/");
        if(EngineManager.isWindows()) wsName = wsName.replaceAll("\\\\", "/");
        if(EngineManager.isWindows()) thisSampleURI = thisSampleURI.replaceAll("\\\\", "/");

        try {
            if ((Integer.parseInt(sParMinPopSize) < 100) || (Integer.parseInt(sParMinPopSize) > 1000000))
                sParMinPopSize= Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinPopSize);
        } catch (Exception e) {
            sParMinPopSize = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinPopSize);
        }

        try {
            if ((Integer.parseInt(sParMinDim) < 1) || (Integer.parseInt(sParMinDim) > 999999))
                sParMinDim= Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinDim);
        } catch (Exception e) {
            sParMinDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinDim);
        }

        try {
          if ((Integer.parseInt(sParMaxDim) < 1) || (Integer.parseInt(sParMaxDim) > 999999))
              sParMaxDim= Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMaxDim);
        } catch (Exception e) {
          sParMaxDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultMaxDim);
        }

        try {
            if ((Integer.parseInt(sParXDim) < 3) || (Integer.parseInt(sParXDim) > 32))
                sParXDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultXDim);
        } catch (Exception e) {
            sParXDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultXDim);
        }

        try {
            if ((Integer.parseInt(sParYDim) < 3) || (Integer.parseInt(sParYDim) > 32))
                sParYDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultYDim);
        } catch (Exception e) {
            sParYDim = Integer.toString(com.flowjo.plugin.ezDAFi.ezDAFi.defaultYDim);
        }

        int minPopSize = com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinPopSize;
        int minDim = com.flowjo.plugin.ezDAFi.ezDAFi.defaultMinDim;
        int maxDim = com.flowjo.plugin.ezDAFi.ezDAFi.defaultMaxDim;
        int xDim = com.flowjo.plugin.ezDAFi.ezDAFi.defaultXDim;
        int yDim = com.flowjo.plugin.ezDAFi.ezDAFi.defaultYDim;
        try {
            minPopSize = Integer.parseInt(sParMinPopSize);
            minDim = Integer.parseInt(sParMinDim);
            maxDim = Integer.parseInt(sParMaxDim);
            xDim = Integer.parseInt(sParXDim);
            yDim = Integer.parseInt(sParYDim);
        } catch (Exception e) {}

        BufferedReader rTemplateReader = null;
        try {
            rTemplateReader = new BufferedReader(new InputStreamReader(scriptStream));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String scriptLine;
        try {
            while((scriptLine = rTemplateReader.readLine()) != null)
            {
                // Added to get runID in parameter - MVP
                //scriptLine = scriptLine.replace("FJ_PARENT_NAME", csvParentFileName);
                scriptLine = scriptLine.replace("FJ_PARM_SAMPLENAME", sampleName);
                scriptLine = scriptLine.replace("FJ_PARM_WSPDIR", wsDir);
                scriptLine = scriptLine.replace("FJ_PARM_WSPNAME", wsName);
                scriptLine = scriptLine.replace("FJ_PARM_SAMPLE_URI", thisSampleURI);
                scriptLine = scriptLine.replace("FJ_PARM_NAME", parameterName);
                scriptLine = scriptLine.replace("FJ_DATA_FILE_PATH", dataFilePath);
                scriptLine = scriptLine.replace("FJ_CSV_OUPUT_FILE", outFileName);
                scriptLine = scriptLine.replace("FJ_GATING_ML_OUTPUT_FILE", gatingMLOutFile.getAbsolutePath());
                //scriptLine = scriptLine.replace("FJ_PAR_SCALE", sParScale);
                scriptLine = scriptLine.replace("FJ_PLOT_STATS", sParPlotStats);
                //scriptLine = scriptLine.replace("FJ_TRANSFORM", sParTrans);
                scriptLine = scriptLine.replace("FJ_BATCH_MODE", sParBatch);
                scriptLine = scriptLine.replace("FJ_PAR_SOM", sParkMeansSom);
                scriptLine = scriptLine.replace("FJ_PAR_PLSDA", sParPLS);
                scriptLine = scriptLine.replace("FJ_PAR_META", sParMeta);
                scriptLine = scriptLine.replace("FJ_PAR_CHILDREN", sParApplyOnChildren);
                scriptLine = scriptLine.replace("FJ_PAR_MINPOPSIZE", sParMinPopSize);
                scriptLine = scriptLine.replace("FJ_MIN_N_PAR", sParMinDim);
                scriptLine = scriptLine.replace("FJ_MAX_N_PAR", sParMaxDim);
                scriptLine = scriptLine.replace("FJ_PAR_XDIM", sParXDim);
                scriptLine = scriptLine.replace("FJ_PAR_YDIM", sParYDim);
                scriptLine = scriptLine.replace("FJ_PAR_APPLY_ON_PREV", sParApplyOnPrev);
                scriptLine = scriptLine.replace("FJ_PAR_ADD_CELLIDS_TO_RESULT", sAddCellIdToResults);
                scriptLine = scriptLine.replace("FJ_POPULATION_NAME", populationName);
                scriptLine = scriptLine.replace("FJ_SAMPLE_NODE_NAME", sampleNodeName);
                scriptLine = scriptLine.replace("FJ_MILLIS_TIME", millisTimeString);
                scriptLine = scriptLine.replace("FJ_OUTPUT_FOLDER", outputFolderString);

                if(scriptLine.contains("FJ_PARAMS_LIST")) {
                    String parListStr = "";
                    for (String parName : parameterNames)
                    {
                        // We don't want the TIME parameter to be in the parameter list given to the R script
                        if(parName.compareToIgnoreCase("TIME") != 0) {
                            if(!parListStr.isEmpty()) parListStr = (new StringBuilder()).append(parListStr).append(",").toString();
                            parListStr = (new StringBuilder()).append(parListStr).append("\"").append(parName).append("\"").toString();
                        }
                    }
                    scriptLine = scriptLine.replaceAll("FJ_PARAMS_LIST", parListStr);
                }
                scriptWriter.append(scriptLine).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(rTemplateReader != null) {
            try { rTemplateReader.close(); }
            catch (Exception e) { e.printStackTrace(); }
        }

        return outFile;
    }

}
