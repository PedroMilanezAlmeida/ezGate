#######################################################################
# Copyright (c) 2020 Pedro Milanez-Almeida, Ph.D., NIAID/NIH
#
# Parts of this code were modified from the source of the packages used here.
# Credits for these parts go to the authors of the packages.
# See list of all packages used below.
#
# License
# The software is distributed under the terms of the
# Artistic License 2.0
# http://www.r-project.org/Licenses/Artistic-2.0
#
# Disclaimer
# This software and documentation come with no warranties of any kind.
# This software is provided "as is" and any express or implied
# warranties, including, but not limited to, the implied warranties of
# merchantability and fitness for a particular purpose are disclaimed.
# In no event shall the  copyright holder be liable for any direct,
# indirect, incidental, special, exemplary, or consequential damages
# (including but not limited to, procurement of substitute goods or
# services; loss of use, data or profits; or business interruption)
# however caused and on any theory of liability, whether in contract,
# strict liability, or tort arising in any way out of the use of this
# software.
######################################################################

#test whether R version older than 4.0.2
Rver.maj <- version$major
Rver.min.1 <- strsplit(x = version$minor, 
                       split = ".",
                       fixed = TRUE,
                       perl = FALSE, 
                       useBytes = FALSE)[[1]][1]
Rver.min.2 <- strsplit(x = version$minor, 
                       split = ".",
                       fixed = TRUE,
                       perl = FALSE, 
                       useBytes = FALSE)[[1]][2]

if(Rver.maj < 4){
  stop(paste0("The plugin cannot run with R versions older than 4.0.2. ",
              "Your version is: ",
              paste0(version$major, ".", version$minor),
              ". Please, update R and try again."))
} else if(Rver.maj == 4 &
          Rver.min.1 < 0){
  stop(paste0("The plugin cannot run with R versions older than 4.0.2. ",
              "Your version is: ",
              paste0(version$major, ".", version$minor),
              ". Please, update R and try again."))
} else if(Rver.maj == 4 &
          Rver.min.1 == 0 &
          Rver.min.2 < 2) {
  stop(paste0("The plugin cannot run with R versions older than 4.0.2. ",
              "Your version is: ",
              paste0(version$major, ".", version$minor),
              ". Please, update R and try again."))
}

tryCatch(suppressMessages(library("BiocManager")),
         error = function(e){
           if (!requireNamespace("BiocManager",
                                 quietly = TRUE))
             install.packages("BiocManager",
                              repos = 'http://cran.us.r-project.org')
           suppressMessages(library("BiocManager"))
         })

Bioc.ver.maj <- strsplit(x = as.character(BiocManager::version()), 
                         split = ".",
                         fixed = TRUE,
                         perl = FALSE, 
                         useBytes = FALSE)[[1]][1]
Bioc.ver.min <- strsplit(x = as.character(BiocManager::version()), 
                         split = ".",
                         fixed = TRUE,
                         perl = FALSE, 
                         useBytes = FALSE)[[1]][2]

if(Bioc.ver.maj < 3){
  stop(paste0("The plugin cannot run with Bioconductor releases older than 3.11. ",
              "Your version is: ",
              BiocManager::version(),
              ". Please, update Bioconductor (visit https://www.bioconductor.org/install/) and try again."))
} else if(Bioc.ver.maj == 3 & 
          Bioc.ver.min < 11){
  stop("The plugin cannot run with Bioconductor releases older than 3.11. ",
       "Your version is: ",
       BiocManager::version(),
       ". Please, update Bioconductor (visit https://www.bioconductor.org/install/) and try again.")
}

tryCatch(suppressMessages(library("flowWorkspace")),
         error = function(e){
           if (!requireNamespace("BiocManager",
                                 quietly = TRUE))
             install.packages("BiocManager",
                              repos = 'http://cran.us.r-project.org')
           BiocManager::install("flowWorkspace",
                                update = FALSE,
                                ask = FALSE)
           suppressMessages(library("flowWorkspace"))
         })
tryCatch(suppressMessages(library("CytoML")),
         error = function(e){
           if (!requireNamespace("BiocManager",
                                 quietly = TRUE))
             install.packages("BiocManager",
                              repos = 'http://cran.us.r-project.org')
           BiocManager::install("CytoML",
                                update = FALSE,
                                ask = FALSE)
           suppressMessages(library("CytoML"))
         })
tryCatch(suppressMessages(library("magrittr")),
         error = function(e){
           install.packages(pkgs =  "magrittr",
                            repos = 'http://cran.us.r-project.org')
           suppressMessages(library("magrittr"))
         })

sessionInfo()

# create R objects with FJ options
wspDir <- "FJ_PARM_WSPDIR"
wspDir
wspName <- "FJ_PARM_WSPNAME"
wspName
fj_millis_time <- "FJ_MILLIS_TIME"
fj_millis_time

# directory where downsampled and concatenated FCS file will be saved:
target.dir <- paste0(wspDir, 
                     "/",
                     "ezGate_",
                     sub(pattern = ".wsp$",
                         replacement = "",
                         x = wspName,
                         fixed = FALSE),
                     "_FCS")
if(!dir.exists(target.dir)){
  dir.create(target.dir)
}

#load wsp file
wspName <- paste0(wspDir, 
                  "/",
                  wspName)
wspName
ws <- CytoML::open_flowjo_xml(wspName,
                              sample_names_from = "sampleNode")

# find FCS files
sampleFCS_paths <- XML::xmlParse(wspName) %>%
  XML::xpathApply(.,
                  file.path("/Workspace/SampleList/Sample","DataSet"),
                  function(x)
                    XML::xmlGetAttr(x,"uri") %>%
                    gsub(pattern = "%20", replacement = " ", x = .) %>%
                    gsub(pattern = "file:", replacement = "", x = .)) %>%
  unlist

cs <- load_cytoset_from_fcs(
  files = normalizePath(sampleFCS_paths),
  #path = ".",
  pattern = NULL,
  phenoData = NULL,
  #  descriptions,
  #  name.keyword,
  transformation = "linearize",
  which.lines = NULL,
  alter.names = FALSE,
  column.pattern = NULL,
  invert.pattern = FALSE,
  decades = 0,
  is_h5 = FALSE,
  min.limit = NULL,
  truncate_max_range = TRUE,
  dataset = NULL,
  emptyValue = TRUE,
  num_threads = 1,
  ignore.text.offset = FALSE,
  sep = "\t",
  as.is = TRUE,
  #  name,
  h5_dir = tempdir(),
  file_col_name = NULL#,
  #  ...
)

# number of FCS files
FCS.n <- length(cs)
# median number of cell in FCS files
med.n <- flowCore::fsApply(cs,
                           dim)[,"events"] %>%
  median(na.rm = TRUE)
# number of cells to extract from each FCS file is the median number of cells in each file divided by the number of files
cells.n <- (med.n/FCS.n) %>%
  floor()
# downsample FCS files
cs.ds <- flowCore::fsApply(cs,
                           function(cf){
                             set.seed(2020)
                             if(dim(cf)[1] > cells.n) { #if FCS file contain fewer cells than target number of cells after downsampling, take all cells from this sample
                               random.cells <- sample(size = cells.n,
                                                      x = dim(cf)[1],
                                                      replace = FALSE)
                             } else {
                               random.cells <- seq(dim(cf)[1])
                             }
                             return(cf[random.cells,])
                           })
# concatenate downsampled FCS files
flowCore::exprs(cs.ds[[1]]) <- flowCore::fsApply(cs.ds,
                                                 flowCore::exprs)
# change keywords that are related to the original sample to "downsampled.fcs"
flowCore::keyword(cs.ds[[1]])[grepl(pattern = sampleNames(cs.ds)[1],
                                    x = flowCore::keyword(cs.ds[[1]]),
                                    fixed = TRUE)] <-
  gsub(pattern = sampleNames(cs.ds)[1],
       replacement = "downsampled.fcs",
       x = flowCore::keyword(cs.ds[[1]])[grepl(pattern = sampleNames(cs.ds)[1],
                                               x = flowCore::keyword(cs.ds[[1]]),
                                               fixed = TRUE)], 
       fixed = TRUE)
# keep only downsampled and concatenated sample
cs.ds <- cs.ds[[1]]
# write downsampled FCS files to the disk with timestamp
flowCore::write.FCS(x = cs.ds,
                    filename = paste0(target.dir,
                                      "/",
                                      fj_millis_time,
                                      ".downsampled.fcs"))
