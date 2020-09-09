//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2020 Pedro Milanez-Almeida, Ph.D., NIAID/NIH
//
// License
// The software is distributed under the terms of the
// Artistic License 2.0
// http://www.r-project.org/Licenses/Artistic-2.0
//
// Disclaimer
// This software and documentation come with no warranties of any kind.
// This software is provided "as is" and any express or implied
// warranties, including, but not limited to, the implied warranties of
// merchantability and fitness for a particular purpose are disclaimed.
// In no event shall the  copyright holder be liable for any direct,
// indirect, incidental, special, exemplary, or consequential damages
// (including but not limited to, procurement of substitute goods or
// services; loss of use, data or profits; or business interruption)
// however caused and on any theory of liability, whether in contract,
// strict liability, or tort arising in any way out of the use of this
// software.
//////////////////////////////////////////////////////////////////////////////
// Based on the FlowSOM plugin
//////////////////////////////////////////////////////////////////////////////

package com.flowjo.plugin.ezGate;

//import com.flowjo.plugin.ezGate.utilities.ExportUtils;
//import com.flowjo.plugin.ezGate.utilities.FJSML;
//import com.flowjo.plugin.ezGate.utils.FilenameUtils;
import com.treestar.flowjo.application.workspace.Workspace;
import com.treestar.flowjo.application.workspace.manager.FJApplication;
import com.treestar.flowjo.application.workspace.manager.WSDocument;
import com.treestar.flowjo.core.Sample;
import com.treestar.flowjo.core.nodes.PopNode;
import com.treestar.flowjo.engine.FEML;
import com.treestar.flowjo.engine.utility.R_Algorithm;
import com.treestar.lib.FJPluginHelper;
import com.treestar.lib.PluginHelper;
import com.treestar.lib.core.ExportFileTypes;
import com.treestar.lib.core.ExternalAlgorithmResults;
import com.treestar.lib.data.StringUtil;
import com.treestar.lib.file.FJFileRef;
import com.treestar.lib.file.FJFileRefFactory;
import com.treestar.lib.fjml.FJML;
import com.treestar.lib.fjml.types.FileTypes;
import com.treestar.lib.gui.FJList;
import com.treestar.lib.gui.GuiFactory;
import com.treestar.lib.gui.HBox;
import com.treestar.lib.gui.numberfields.RangedIntegerTextField;
import com.treestar.lib.gui.panels.FJLabel;
import com.treestar.lib.gui.swing.FJCheckBox;
import com.treestar.lib.gui.swing.FJComboBox;
import com.treestar.lib.parsing.interpreter.CSVReader;
import com.treestar.lib.parsing.interpreter.ParseUtil;
import com.treestar.lib.xml.SElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.flowjo.plugin.ezGate.RScriptFlowCalculator.fOutFile;
import static com.flowjo.plugin.ezGate.RScriptFlowCalculator.fOutFileLastLines;
import static java.lang.System.currentTimeMillis;

public class ezGate extends R_Algorithm {

    private static final String pluginVersion = "0.1";
    public static String pluginName = "ezGate";
    public static boolean runAgain = false;
    public static boolean nameSet = false;
    //public static String runID = "";

    public static final String One = "1";
    public static final String Zero = "0";
    public static final String True = "true";
    public static final String False = "false";
    //public static final String cellIdParName = "CellId";


    //private RangedIntegerTextField fDimXField = null, fDimYField = null;
    //private RangedIntegerTextField fMinPopSizeField = null;
    //private RangedIntegerTextField fMinDimField = null;
    //private RangedIntegerTextField fMaxDimField = null;
    //private FJComboBox fApplyOnPrevCombo = null;
    //private FJCheckBox fScaleOptionCheckbox = null;
    //private FJCheckBox fPlotStatsOptionCheckbox = null;
    //private FJCheckBox fTransOptionCheckbox = null;
    //private FJCheckBox fBatchOptionCheckbox = null;
    //private FJCheckBox fShowRScriptCheckbox = null;
    //private FJCheckBox fKMeansSomOptionCheckbox = null;
    //private FJCheckBox fPLSOptionCheckbox = null;
    //private FJCheckBox fMetaOptionCheckbox = null;
    //private FJCheckBox fApplyOnChildrenCheckbox = null;

    private static final int fixedLabelWidth = 130;
    private static final int fixedFieldWidth = 75;
    private static final int fixedLabelHeigth = 25;
    private static final int fixedFieldHeigth = 25;
    private static final int fixedToolTipWidth = 300;
    private static final int fixedComboWidth = 150;
    private static final int hSpaceHeigth = 5;
    private SElement fsElement = null;
    private static final String space = " ";

    //private static final String applyOnPrevLabel = "Apply on map";
    //private static final String applyOnPrevTooltip = "If you have executed ezGate before, you can apply new data to a map generated by previous runs of ezGate. A new ezGate object will be created with the same grid but new mapping, node sizes and mean values.";
    //private static final String dimXLabel = "SOM grid size (W x H)";
    //private static final String dimXTooltip = "Width of the grid for building the self-organizing map.";
    //private static final String dimYTooltip = "Height of the grid for building the self-organizing map.";
    //private static final String minPopSizeLabel = "min # of events";
    //private static final String mustBeMinPopSizeLabel = "(min # of events must be larger than SOM grid size W x H!)";
    //private static final String minPopSizeTooltip = "Smallest number of cells to apply ezGate on.";
    //private static final String minDimLabel = "# of dimensions: min";
    //private static final String maxDimLabel = "hidden dimensions:";
    //private static final String mustBeMinDimLabel = "(use min AND max = 1 for auto-selection)";
    //private static final String minDimTooltip = "Min and max number of dimensions to apply ezGate on. High number of dimensions lead to high dimensional noise, low number of dimensions lead to no improvement over manual gate.";
    //private static final String maxDimTooltip = "Expand your bi-dimensional gates with hidden dimensions. ezGate learns the most informative hidden dimensions from the data for every downstream gate. This number determines how many hidden dimensions to include in the dim-expanded gates.";

    //private static final String orPerformezGateLabel = "or perform new ezGate.";
    //private static final String scaleLabel = "Scale parameters to mean = 0 and sd = 1 (use with care)";
    //private static final String scaleTooltip = "Should the data be scaled prior to clustering?";
    //private static final String plotStatsLabel = "Experimental: save plots and stats (can be slow).";
    //private static final String plotStatsTooltip = "Should side-by-side plots of manual and ezGate gates be automatically saved as well as frequency of parents and counts?";
    //private static final String transLabel = "Apply FJ data transformation.";
    //private static final String transTooltip = "If not working with raw FCS files but pre-processed CSV files from other applications such as CITE-seq or histo-cytometry, the data may already have been transformed and this box should be unchecked.";
    //private static final String batchLabel = "Advanced (results not re-imported to FlowJo; batch mode).";
    //private static final String batchTooltip = "ezGate all samples, plot back-gating results and continue analysis in R. Gates will not be re-imported to FlowJo.";
    //private static final String showRScriptLabel = "Show RScript (.txt format) upon completion.";
    //private static final String showRScriptTooltip = "Show the resulting RScript file (in .txt format), created doing the ezGate process.";
    //private static final String kMeansSomLabel = "Cluster with self organizing maps (SOM; uncheck for k-means).";
    //private static final String kMeansSomTooltip = "Which algorithm should be used for clustering?";
    //private static final String PLSLabel = "Experimental: cluster on PLS-DA latent variables.";
    //private static final String PLSTooltip = "Should the data be pre-processed with PLS-DA prior to clustering?";
    //private static final String metaLabel = "Experimental: meta-cluster centroids (can be slow).";
    //private static final String metaTooltip = "Should the centroids be meta-clustered prior to gating? Intended to stabilize gating results in the presence of technical variation.";
    //private static final String applyOnChildrenLabel = "<html>Apply on children only."
    //      + "<br>(otherwise, recursive).";
    //private static final String applyOnChildrenTooltip = "If checked, ezGate will refine only the children of the selected population. If unchecked, all children of children will be refined recursively (i.e., all sub-populations downstream of the selected one).";

    //public static final String scaleOptionName = "scale";
    //public static final String plotStatsOptionName = "plotStats";
    //public static final String transOptionName = "trans";
    //public static final String batchOptionName = "batch";
    //public static final String showRScriptOptionName = "RScript";
    //public static final String kMeansSomOptionName = "kMeansSom";
    //public static final String PLSOptionName = "PLS";
    //public static final String metaOptionName = "spec";
    //public static final String applyOnChildrenOptionName = "childrenOnly";
    //public static final String xDimOptionName = "xdim";
    //public static final String yDimOptionName = "ydim";
    //public static final String minPopSizeOptionName = "minPopSize";
    //public static final String minDimOptionName = "minDim";
    //public static final String maxDimOptionName = "maxDim";
    //public static final String applyOnPrevOptionName = "applyOn"; // "None" or file path to an RData file with a ezGate object
    //public static final String pluginFolderAttName = "pluginFolder";
    //public static final String sampleURISlot = "sampleURI";
    //public static final String samplePopNodeSlot = "samplePopNode";
    //public static final String sampleFileSlot = "sampleFile";

    public static final String RDataFileExtension = ".RData";
    public static final String RDataFileSuffix = ".csv.ezGate.csv.RData";
    public static final String CSVwithParsFileSuffix = ".csv.ezGate.csv.pars.csv";

    //public static final int defaultXDim = 10;
    //public static final int defaultYDim = 10;
    //public static final int defaultMinPopSize = 50;
    //public static final int defaultMinDim = 1;
    //public static final int defaultMaxDim = 3;
    //public static final String defaultApplyOnPrev = "None";
    //public static final boolean defaultScale = false;
    //public static final boolean defaultPlotStats = false;
    //public static final boolean defaultTrans = true;
    //public static final boolean defaultBatch = false;
    //public static final boolean defaultShowRScript = true;
    //public static final boolean defaultKMeansSom = true;
    //public static final boolean defaultPLS = false;
    //public static final boolean defaultMeta = false;
    //public static final boolean defaultApplyOnChildren = false;

    //private boolean fScale = defaultScale;
    //private boolean fPlotStats = defaultPlotStats;
    //private boolean fTrans = defaultTrans;
    //private boolean fBatch = defaultBatch;
    //private boolean fShowRScript = defaultShowRScript;
    //private boolean fKMeansSom = defaultKMeansSom;
    //private boolean fPLS = defaultPLS;
    //private boolean fMeta = defaultMeta;
    //private boolean fApplyOnChildren = defaultApplyOnChildren;
    //private int fndimx = defaultXDim, fndimy = defaultYDim;
    //private int fnMinPopSize = defaultMinPopSize;
    //private int fnMinDim = defaultMinDim;
    //private int fnMaxDim = defaultMaxDim;
    //private String fAnalysisPathSampleURI = "test1";
    //private String fAnalysisPathSamplePopNode = "test2";
    //private String fAnalysisPathSampleFile = "test3";

    private static final String channelsLabelLine0 = "All samples in this workspace will be downsampled and concatenated";
    private static final String channelsLabelLine1 = "to a single FCS file that will be saved in the same folder as the";
    private static final String channelsLabelLine2 = "workspace folder, in sub-folder 'ezGate_workspaceName_FCS'.";
    private static final String channelsLabelLine3 = "";

    //private static final String pathToScriptLabelLine1 = "The RScript created in this analysis is located in the same folder as";
    //private static final String pathToScriptLabelLine2 = "the FJ workspace, under /WORKSPACE_NAME/ezGate/RScript.'numbers'.R.txt,";
    //private static final String pathToScriptLabelLine3 = "where 'numbers' is time of creation in milliseconds.";
    //private static final String pathToScriptLabelLine4 = "";

    private static final String citingLabelLine1 = "Required: if using ezGate, cite";
    private static final String citingLabelLine2 = "ADD CITATION LINE 1";
    private static final String citingLabelLine3 = "ADD CITATION LINE 2";
    private static final String citingLabelLine4 = "ADD CITATION LINE 3";

    protected static final String sIconName = "images/ezGateIcon.png";

    private static Icon myIcon = null;
    private static final String Failed = "Failed";

    public ezGate() {
        super(pluginName);
    }

    public String getVersion() {
        return (pluginVersion);
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public Icon getIcon() {
              if (myIcon == null) {
    URL url = ezGate.class.getResource(getIconName());
          if (url != null)
              myIcon = new ImageIcon(url);
      }
      return myIcon;
    }

    @Override
    protected String getIconName() {
      return sIconName;
    }

    //@Override
    //protected boolean showClusterInputField() {
    //  return false;
    //}

    @Override
    public ExportFileTypes useExportFileType() {
      return ExportFileTypes.CSV_CHANNEL;
    }

    public ExternalAlgorithmResults invokeAlgorithm(SElement fcmlQueryElement, File sampleFile, File outputFolder) {
        ExternalAlgorithmResults results = new ExternalAlgorithmResults();

        //String savedSampleURI = fOptions.get(sampleURISlot);
        //String savedSamplePopNode = fOptions.get(samplePopNodeSlot);

        //System.out.println("savedSampleURI: " + savedSampleURI);
        //System.out.println("savedSamplePopNode: " + savedSamplePopNode);

        //String thisSampleURI = FJPluginHelper.getSampleURI(fcmlQueryElement);
        //String thisSamplePopNode;
        //try{
        //  thisSamplePopNode = FJPluginHelper.getParentPopNode(fcmlQueryElement).getName();
        //} catch (Exception e) {
        //  thisSamplePopNode = "__pluginCalledOnRoot__";
        //}

        //String thisSamplePopNode = FJPluginHelper.getParentPopNode(fcmlQueryElement).getName();

        //System.out.println("thisSampleURI: " + thisSampleURI);
        //System.out.println("thisSamplePopNode: " + thisSamplePopNode);

        //boolean checkPrevRun = savedSamplePopNode.equals(thisSamplePopNode) && !savedSampleURI.equals(thisSampleURI);

        //System.out.println("checkPrevRun: " + checkPrevRun);

        //System.out.println("runAgain: " + runAgain);

        //if(checkPrevRun){
        //  runAgain = true;
        //}

        //System.out.println("runAgain: " + runAgain);

        // RunAgain is set to true when the user double clicks on the plugin node, this avoids the recalculation on update.
        if (!runAgain) {
            return results;
        }
        // If the plugin fails, we need to avoid the recalculation as it might not get fixed anyways.
        runAgain = false;

        //System.out.println("!sampleFile.exists(): " + !sampleFile.exists());

        // trying to separate each run using time in millisecond
        // in case you want to add time to separate between runs
        long millisTime = currentTimeMillis();
        System.out.println("millisTime: " + millisTime);
        String outputFolderMillisTimePath = outputFolder.getAbsolutePath() + File.separator + millisTime;
        System.out.println("outputFolderMillisTimePath: " + outputFolderMillisTimePath);
        File outputFolderMillisTimeFile = new File(outputFolderMillisTimePath);
        new File(outputFolderMillisTimePath).mkdirs();

        //if (!sampleFile.exists()) {
        // results.setErrorMessage("Input file did not exist"); // We purposely don't want to set the error as there may be undesirable side-effects
        //  JOptionPane.showMessageDialog(null, "Input file did not exist", "ezGate error", JOptionPane.ERROR_MESSAGE);
        //  results.setWorkspaceString(ezGate.Failed);
        //  return results;
        //} else {
        // Let's force recalculation all the time because it's relatively quick and we don't seem to handle
        // checkUseExistingFiles well (i.e., if input settings change a bit, we still tend to return previous
        // results instead of recalculating
        // checkUseExistingFiles(fcmlQueryElement);
        //fUseExistingFiles = false;

        //fOptions.put(sampleFileSlot, sampleFile.getAbsolutePath());
        //fOptions.put(sampleURISlot, thisSampleURI);
        //fOptions.put(samplePopNodeSlot, thisSamplePopNode);


        //save workspace before running plugin
        Sample sample = FJPluginHelper.getSample(fcmlQueryElement);
        Workspace workspace = sample.getWorkspace();
        WSDocument wsd = workspace.getDoc();
        //wsd.save();


        //get workspace directory and path to enable working with acs files
        String wsDir = wsd.getWorkspaceDirectory().getAbsolutePath();
        String wsName = wsd.getFilename();
        //String outputFolderPath = wsDir + File.separator + "ezGate";

        //Create variable with names of parameters
        //List<String> parameterNames = preprocessCompParameterNames();

        //get sample name
        //String sampleName = sampleFile.getName();

        // Get gate name and the parent popnode
        //PopNode popNode = FJPluginHelper.getParentPopNode(fcmlQueryElement);
        //PopNode parentPopNode = popNode.getParentPop();
        //if (parentPopNode == null) { // This means the current parent node is the root sample, if it is just take the sample node.
        //    parentPopNode = sample.getSampleNode();
        //}

        // tried to add ability to run ezGate on parent of selected pop: FAILED (see line 283)
        //List params = new ArrayList();
        //params.add("EventNumberDP");

        //File csvParentFile = ExportUtils.exportParameters(parentPopNode, params, outputFolder, sampleName);
        //String csvParentFileName = sampleName + ".PARENT" + FJSML.FORMATS.FILE.CSV.EXTENSION;

        //Get name of .FCS file
        //PopNode sampleNode = sample.getSampleNode();

        //System.out.println("sampleName: " + sampleName);

        ezGateRFlowCalc calculator = new ezGateRFlowCalc();
        // Added the population node
        File ezGateResult = calculator.runezGate(wsName, wsDir, millisTime, outputFolderMillisTimeFile.getAbsolutePath());
        calculator.deleteScriptFile();
        checkROutFile(calculator);

        //This is a workaround for the bug that FlowJo is not showing errors in R:
        //Try to read the results (import derived parameters and gatingML files), and, if requested, print the Rscript.
        //If this fails, print only the last 30 lines of the Rscript to make the error visible to the user.
        try {
            // Added to avoid issue with sub pops in FlowJo.

            // the following code was used to try to add the capability of running flowjo on selected population if it has no child.
            // i.e. run ezGate on parent of selected pop and refine selected pop.
            // IT FAILED!
            // The problem seems to be with "ExternalAlgorithmResults results", which apparently sends the results of the plugin back
            // to the selected population but we need it to be sent back to its parent :'(

            // detect whether ezGate was run on selected pop or on parent
            //int nLinesFJOut = countLinesNew(sampleFile.getAbsolutePath());
            //int nLinesPluginOut = countLinesNew(ezGateResult.getAbsolutePath());
            //System.out.println(nLinesFJOut);
            //System.out.println(nLinesPluginOut);

            // try to make mergeCSVFile work on parent rather than selected pop: FAIL
            //if (nLinesFJOut == nLinesPluginOut) {
            //mergeCSVFile(fcmlQueryElement, results, ezGateResult, sampleFile, outputFolder);
            //} else {
            //    mergeCSVFile(fcmlQueryElement.getParentSElement(), results, ezGateResult, csvParentFile, outputFolder);
            //}

            //}
            System.out.println(ezGateResult.getAbsolutePath());
            //List<Float> values = extractUniqueValuesForParameter(ezGateResult);

            //create a string which represents the gating-ml file
            //String xmlEnding = sampleFile.getName() + ".gating-ml2.xml";

            //create a filter to find the gating-ml file in the folder
            //FilenameFilter xmlFileFilter = (dir, name) -> name.endsWith(xmlEnding);

            //File[] xmlFiles = outputFolder.listFiles(xmlFileFilter);

            //for (File xmlFile : xmlFiles)
            //{
            //  String gatingML = readGatingMLFile(xmlFile);
            //  results.setGatingML(gatingML);
            //}

            //String sParShowRScript = fOptions.get(showRScriptOptionName);
            //if (sParShowRScript == null || sParShowRScript.isEmpty() || com.flowjo.plugin.ezGate.ezGate.One.equals(sParShowRScript) || com.flowjo.plugin.ezGate.ezGate.True.equals(sParShowRScript)){
            //  fShowRScript = true; // TRUE is the default
            //} else {
            //  fShowRScript = false;
            //}

            //if (fShowRScript){
              try {
                  Desktop.getDesktop().open(fOutFile);
              } catch (IOException e) {
                  e.printStackTrace();
              }
                //}

        } catch (Exception error) {
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter(fOutFileLastLines));
                writer.write(tail2(fOutFile, 10));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Desktop.getDesktop().open(fOutFileLastLines);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        for (File f : outputFolder.listFiles()) {
            if (f.getName().endsWith(".ExtNode.csv")) {
                f.delete(); // may fail mysteriously - returns boolean you may want to check
            }
        }

        return results;
        //}
    }

    @Override
    protected List<Component> getPromptComponents(SElement fcmlElem, SElement algorithmElement, List<String> parameterNames) {
        fsElement = fcmlElem;
        List<Component> ret = super.getPromptComponents(fcmlElem, algorithmElement, parameterNames);

        runAgain = true;
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Component> getPromptComponents(SElement selement, List<String> list) {
        ArrayList<Component> componentList = new ArrayList<>();

        FJLabel fjLabel0 = new FJLabel(channelsLabelLine0);
        FJLabel fjLabel1 = new FJLabel(channelsLabelLine1);
        FJLabel fjLabel2 = new FJLabel(channelsLabelLine2);
        FJLabel fjLabel3 = new FJLabel(channelsLabelLine3);

        componentList.add(fjLabel0);
        componentList.add(fjLabel1);
        componentList.add(fjLabel2);
        componentList.add(fjLabel3);

        componentList.add(addFlowJoParameterSelector(list));

        fsElement = selement;

        String sampleFile = "";

        FJLabel hSpaceLabelCiting = new FJLabel("");
        GuiFactory.setSizes(hSpaceLabelCiting, new Dimension(fixedLabelWidth, hSpaceHeigth));
        componentList.add(hSpaceLabelCiting);

        //componentList.add(new FJLabel(pathToScriptLabelLine1));
        //componentList.add(new FJLabel(pathToScriptLabelLine2));
        //componentList.add(new FJLabel(pathToScriptLabelLine3));
        //componentList.add(new FJLabel(pathToScriptLabelLine4));

        componentList.add(new FJLabel(citingLabelLine1));
        componentList.add(new FJLabel(citingLabelLine2));
        componentList.add(new FJLabel(citingLabelLine3));
        componentList.add(new FJLabel(citingLabelLine4));

        return componentList;
    }

    public JScrollPane addFlowJoParameterSelector(List<String> parameters) {

        if (fParameterNameList == null) {
            DefaultListModel dlm = new DefaultListModel();
            for (int i = 0; i < parameters.size(); i++) {
                dlm.add(i, parameters.get(i));
            }
            fParameterNameList = new FJList(dlm);
            fParameterNameList.setSelectionMode(2);
        }

        JScrollPane scrollableList = new JScrollPane(fParameterNameList);
        int[] indexes = new int[fParameterNameList.getModel().getSize()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        fParameterNameList.setSelectedIndices(indexes);
        //return scrollableList;
        return null;
    }

    //trying to open .R.txt file
    public void open(File document)
            throws IOException {
        Desktop.getDesktop().open(document);
    }

    @Override
    protected void extractPromptOptions() {
        fOptions = new HashMap<>();
        fParameterNames = new ArrayList<>();
    }

    public String tail2( File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile( file, "src/ezGate/com.flowjo.plugin.ezGate.r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if( readByte == 0xD ) {
                    if (filePointer < fileLength-1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    public static int countLinesNew(String filename) throws IOException { //https://stackoverflow.com/a/453067
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }


}
