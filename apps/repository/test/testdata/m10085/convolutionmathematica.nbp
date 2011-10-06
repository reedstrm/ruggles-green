(* Content-type: application/mathematica *)

(*** Wolfram Notebook File ***)
(* http://www.wolfram.com/nb *)

(* CreatedBy='Mathematica 7.0' *)

(*CacheID: 234*)
(* Internal cache information:
NotebookFileLineBreakTest
NotebookFileLineBreakTest
NotebookDataPosition[       145,          7]
NotebookDataLength[     43864,        877]
NotebookOptionsPosition[     27621,        586]
NotebookOutlinePosition[     43950,        879]
CellTagsIndexPosition[     43907,        876]
WindowFrame->Normal*)

(* Beginning of Notebook Content *)
Notebook[{
Cell[BoxData[
 TagBox[
  StyleBox[
   DynamicModuleBox[{$CellContext`f$$ = 0& , $CellContext`g$$ = 0& , 
    Typeset`show$$ = True, Typeset`bookmarkList$$ = {}, 
    Typeset`bookmarkMode$$ = "Menu", Typeset`animator$$, Typeset`animvar$$ = 
    1, Typeset`name$$ = "\"untitled\"", Typeset`specs$$ = {{
      Hold[
       Row[{"flip f ", 
         Checkbox[
          Dynamic[$CellContext`flipf], {{1, -1, 0, 1, 2}, {0, 1, 1, -1, 2}}], 
         "flip g ", 
         Checkbox[
          Dynamic[$CellContext`flipg], {{0, 1, 1, -1, 1}, {1, -1, 0, 1, 1}}], 
         Style["    T", 12, 
          GrayLevel[0]], 
         Slider[
          Dynamic[$CellContext`tt], {-4, 4}], 
         InputField[
          Dynamic[$CellContext`tt], ImageSize -> 40]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {{
       Hold[$CellContext`f$$], 0& , 
       Style["density function f", 
        RGBColor[0.24720000000000017`, 0.24, 0.6]]}, {($CellContext`amp1 
        Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
         UnitStep[# + $CellContext`delay1] - 
         UnitStep[# + $CellContext`delay1 - $CellContext`length1])& ) -> 
       "Cosine", ($CellContext`amp1 (UnitStep[# - $CellContext`delay1] - 
         UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
       "Pulse", ($CellContext`amp1 # (UnitStep[# - $CellContext`delay1] - 
         UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
       "Linear", (($CellContext`amp1 #^2) (UnitStep[# - $CellContext`delay1] - 
         UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
       "Parabolic", ($CellContext`amp1 
        Exp[$CellContext`phase1 #] (UnitStep[# - $CellContext`delay1] - 
         UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
       "Exponential", (
        Abs[$CellContext`amp1 
         Exp[$CellContext`phase1 #^2] (UnitStep[# - 2 $CellContext`delay1] - 
          UnitStep[# - 2 ($CellContext`delay1 + $CellContext`length1)])]& ) -> 
       "Gaussian"}}, {{
       Hold[$CellContext`g$$], 0& , 
       Style["density function g", 
        RGBColor[0.6, 0.24, 0.4428931686004542]]}, {($CellContext`amp2 
        Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
         UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
       "Cosine", ($CellContext`amp2 (UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
       "Pulse", ($CellContext`amp2 # (UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
       "Linear", (($CellContext`amp2 #^2) (UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
       "Parabolic", ($CellContext`amp2 
        Exp[$CellContext`phase2 #] (UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
       "Exponential", (
        Abs[$CellContext`amp2 
         Exp[$CellContext`phase2 #^2] (UnitStep[# - 2 $CellContext`delay2] - 
          UnitStep[# - 2 ($CellContext`delay2 + $CellContext`length2)])]& ) -> 
       "Gaussian"}}, {
      Hold[
       Row[{
         Button[
         "Reset", $CellContext`flipg = {0, 1, 1, -1, 
            2}; $CellContext`flipf = {1, -1, 0, 1, 1}; $CellContext`tt = 0; 
          Clear[$CellContext`z]; $CellContext`f$$ = 0; $CellContext`g$$ = 
           0; $CellContext`amp1 = 1; $CellContext`amp2 = 
           1; $CellContext`length1 = 1; $CellContext`length2 = 
           1; $CellContext`delay1 = 0; $CellContext`delay2 = 
           0; $CellContext`period1 = 1; $CellContext`period2 = 
           1; $CellContext`phase1 = -1; $CellContext`phase2 = -1]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Dynamic[
        Row[{
          Style["f =  ", 20, 
           ColorData[1, 1]], 
          Text[
           ToString[
            $CellContext`f$$[
            Part[$CellContext`flipf, 1] $CellContext`length1 + 
             Part[$CellContext`flipf, 
                2] ($CellContext`tt - $CellContext`tau)], StandardForm]]}, 
         ImageSize -> {400, 20}]]], Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Column[{
         Row[{
           Style["Amp1", 
            GrayLevel[0]], 
           Slider[
            Dynamic[$CellContext`amp1], {-2, 2}], 
           InputField[
            Dynamic[$CellContext`amp1], ImageSize -> 40]}], 
         Row[{
           Style["Length1", 
            RGBColor[1, 0, 0]], 
           Slider[
            Dynamic[$CellContext`length1], {0.001, 2}], 
           InputField[
            Dynamic[$CellContext`length1], ImageSize -> 50]}], 
         Row[{
           Style["Delay1", 
            RGBColor[0, 0, 1]], 
           Slider[
            Dynamic[$CellContext`delay1], {-2, 2}], 
           InputField[
            Dynamic[$CellContext`delay1], ImageSize -> 40]}], 
         Row[{
           Style["Period1", 
            RGBColor[1, 0.5, 0]], 
           Slider[
            Dynamic[$CellContext`period1], {1.*^-13, 2}], 
           InputField[
            Dynamic[$CellContext`period1], ImageSize -> 55]}], 
         Row[{
           Style["Phase1", 
            RGBColor[0.5, 0, 0.5]], 
           Slider[
            Dynamic[$CellContext`phase1], {-5, 1}], 
           InputField[
            Dynamic[$CellContext`phase1], ImageSize -> 50]}]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Dynamic[
        Row[{
          Style["g =  ", 20, 
           ColorData[1, 2]], 
          Text[
           ToString[
            $CellContext`g$$[
            Part[$CellContext`flipg, 1] $CellContext`length2 + 
             Part[$CellContext`flipg, 2] $CellContext`z], StandardForm]]}, 
         ImageSize -> {400, 20}]]], Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Amp2", 
          GrayLevel[0]], 
         Slider[
          Dynamic[$CellContext`amp2], {-2, 2}], 
         InputField[
          Dynamic[$CellContext`amp2], ImageSize -> 40]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Length2", 
          RGBColor[1, 0, 0]], 
         Slider[
          Dynamic[$CellContext`length2], {0.001, 2}], 
         InputField[
          Dynamic[$CellContext`length2], ImageSize -> 50]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Delay2", 
          RGBColor[0, 0, 1]], 
         Slider[
          Dynamic[$CellContext`delay2], {-2, 2}], 
         InputField[
          Dynamic[$CellContext`delay2], ImageSize -> 40]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Period2", 
          RGBColor[1, 0.5, 0]], 
         Slider[
          Dynamic[$CellContext`period2], {1.*^-13, 2}], 
         InputField[
          Dynamic[$CellContext`period2], ImageSize -> 55]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Phase2", 
          RGBColor[0.5, 0, 0.5]], 
         Slider[
          Dynamic[$CellContext`phase2], {-5, 1}], 
         InputField[
          Dynamic[$CellContext`phase2], ImageSize -> 50]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Button["Set Plot Heights", $CellContext`plotheight = Which[Abs[
                Max[$CellContext`fdata]] Abs[
                Max[$CellContext`gdata]] >= Abs[
               Max[$CellContext`fdata]], 1.2 Max[$CellContext`fdata] 
             Max[$CellContext`gdata], Abs[
                Max[$CellContext`fdata]] Abs[
                Max[$CellContext`gdata]] >= Abs[
               Max[$CellContext`gdata]], 1.2 Max[$CellContext`fdata] 
             Max[$CellContext`gdata], Abs[
               Max[$CellContext`fdata]] > Abs[
                Max[$CellContext`fdata]] Abs[
                Max[$CellContext`gdata]], 1.2 Max[$CellContext`fdata], Abs[
               Max[$CellContext`gdata]] > Abs[
                Max[$CellContext`fdata]] Abs[
                Max[$CellContext`gdata]], 1.2 
             Max[$CellContext`gdata]]; $CellContext`convolveheight = 1.2 Max[
              Abs[
              ListConvolve[$CellContext`fdata, $CellContext`gdata, {1, -1}, 
                 0]/100]]]}]], Manipulate`Dump`ThisIsNotAControl}}, 
    Typeset`size$$ = {400., {148., 459.}}, Typeset`update$$ = 0, 
    Typeset`initDone$$, Typeset`skipInitDone$$ = 
    False, $CellContext`f$436758$$ = False, $CellContext`g$436759$$ = False}, 
    DynamicBox[Manipulate`ManipulateBoxes[
     1, StandardForm, 
      "Variables" :> {$CellContext`f$$ = 0& , $CellContext`g$$ = 0& }, 
      "ControllerVariables" :> {
        Hold[$CellContext`f$$, $CellContext`f$436758$$, False], 
        Hold[$CellContext`g$$, $CellContext`g$436759$$, False]}, 
      "OtherVariables" :> {
       Typeset`show$$, Typeset`bookmarkList$$, Typeset`bookmarkMode$$, 
        Typeset`animator$$, Typeset`animvar$$, Typeset`name$$, 
        Typeset`specs$$, Typeset`size$$, Typeset`update$$, Typeset`initDone$$,
         Typeset`skipInitDone$$}, "Body" :> Panel[
        Dynamic[$CellContext`fdata = {{
             Table[
              $CellContext`f$$[
              Part[$CellContext`flipf, 3] $CellContext`length1 + 
               Part[$CellContext`flipf, 4] $CellContext`i], {$CellContext`i, 
               0, $CellContext`length1, 0.01}]}, {
             Table[
              $CellContext`f$$[
              Part[$CellContext`flipf, 1] $CellContext`length1 + 
               Part[$CellContext`flipf, 2] $CellContext`i], {$CellContext`i, 
               0, $CellContext`length2, 0.01}]}}; $CellContext`gdata = {{
             Table[
              $CellContext`g$$[
              Part[$CellContext`flipg, 1] $CellContext`length1 + 
               Part[$CellContext`flipg, 2] $CellContext`i], {$CellContext`i, 
               0, $CellContext`length1, 0.01}]}, {
             Table[
              $CellContext`g$$[
              Part[$CellContext`flipg, 3] $CellContext`length2 + 
               Part[$CellContext`flipg, 4] $CellContext`i], {$CellContext`i, 
               0, $CellContext`length2, 0.01}]}}; ListLinePlot[ListConvolve[
             Part[$CellContext`fdata, 
              Part[$CellContext`flipf, 5]], 
             Part[$CellContext`gdata, 
              Part[$CellContext`flipg, 5]], {1, -1}, 0]/100, 
           DataRange -> {0, $CellContext`length2 + $CellContext`length1}, 
           PlotRange -> {-$CellContext`convolveheight, \
$CellContext`convolveheight}, Filling -> Axis, FillingStyle -> Directive[
             Opacity[0.5], Blue], AxesLabel -> {$CellContext`\[Tau], None}, 
           Epilog -> {{
              ColorData[1, 1], Dashed, 
              Line[{{$CellContext`tt, 0}, {$CellContext`tt, -0.05}}]}, 
             Text[
              Style["t", 
               ColorData[1, 1], 12], {$CellContext`tt, -0.3}]}, PlotLabel -> 
           Row[{
              Style["Convolution Output", 
               ColorData[1, 4]]}]]], 
        Panel[
         Dynamic[
          Plot[{
            $CellContext`f$$[
            Part[$CellContext`flipf, 1] $CellContext`length1 + 
             Part[$CellContext`flipf, 
                2] ($CellContext`tt - $CellContext`tau)], 
            $CellContext`g$$[
            Part[$CellContext`flipg, 1] $CellContext`length2 + 
             Part[$CellContext`flipg, 2] $CellContext`tau], $CellContext`f$$[
             Part[$CellContext`flipf, 1] $CellContext`length1 + 
              Part[$CellContext`flipf, 
                 2] ($CellContext`tt - $CellContext`tau)] $CellContext`g$$[
             Part[$CellContext`flipg, 1] $CellContext`length2 + 
              Part[$CellContext`flipg, 
                 2] $CellContext`tau]}, {$CellContext`tau, -5, 5}, 
           AxesLabel -> {$CellContext`\[Tau], None}, Epilog -> {{
              ColorData[1, 1], Dashed, 
              Line[{{$CellContext`tt, 0}, {$CellContext`tt, 0.5}}]}, 
             Text[
              Style["T", 
               ColorData[1, 1], 12], {$CellContext`tt, -0.1}]}, 
           Filling -> {3 -> Axis}, FillingStyle -> LightGray, PlotLabel -> 
           Row[{
              Style["f[\[Tau]]", 
               ColorData[1, 1]], ", ", 
              Style["g[z]", 
               ColorData[1, 2]], ", and ", 
              Style["f[\[Tau]]g[z]", 
               ColorData[1, 3]]}], 
           PlotRange -> {-$CellContext`plotheight, $CellContext`plotheight}]],
          ImageSize -> {400, 300}], ImageSize -> {400, 300}], 
      "Specifications" :> {
        Row[{"flip f ", 
          Checkbox[
           Dynamic[$CellContext`flipf], {{1, -1, 0, 1, 2}, {0, 1, 1, -1, 2}}],
           "flip g ", 
          Checkbox[
           Dynamic[$CellContext`flipg], {{0, 1, 1, -1, 1}, {1, -1, 0, 1, 1}}], 
          Style["    T", 12, 
           GrayLevel[0]], 
          Slider[
           Dynamic[$CellContext`tt], {-4, 4}], 
          InputField[
           Dynamic[$CellContext`tt], ImageSize -> 40]}], {{$CellContext`f$$, 
          0& , 
          Style["density function f", 
           RGBColor[0.24720000000000017`, 0.24, 0.6]]}, {($CellContext`amp1 
           Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
            UnitStep[# + $CellContext`delay1] - 
            UnitStep[# + $CellContext`delay1 - $CellContext`length1])& ) -> 
          "Cosine", ($CellContext`amp1 (UnitStep[# - $CellContext`delay1] - 
            UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
          "Pulse", ($CellContext`amp1 # (UnitStep[# - $CellContext`delay1] - 
            UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
          "Linear", (($CellContext`amp1 #^2) (
            UnitStep[# - $CellContext`delay1] - 
            UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
          "Parabolic", ($CellContext`amp1 
           Exp[$CellContext`phase1 #] (UnitStep[# - $CellContext`delay1] - 
            UnitStep[# - $CellContext`delay1 - $CellContext`length1])& ) -> 
          "Exponential", (
           Abs[$CellContext`amp1 
            Exp[$CellContext`phase1 #^2] (UnitStep[# - 2 $CellContext`delay1] - 
             UnitStep[# - 
              2 ($CellContext`delay1 + $CellContext`length1)])]& ) -> 
          "Gaussian"}}, {{$CellContext`g$$, 0& , 
          Style["density function g", 
           RGBColor[0.6, 0.24, 0.4428931686004542]]}, {($CellContext`amp2 
           Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
            UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
          "Cosine", ($CellContext`amp2 (UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
          "Pulse", ($CellContext`amp2 # (UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
          "Linear", (($CellContext`amp2 #^2) (
            UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
          "Parabolic", ($CellContext`amp2 
           Exp[$CellContext`phase2 #] (UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ) -> 
          "Exponential", (
           Abs[$CellContext`amp2 
            Exp[$CellContext`phase2 #^2] (UnitStep[# - 2 $CellContext`delay2] - 
             UnitStep[# - 
              2 ($CellContext`delay2 + $CellContext`length2)])]& ) -> 
          "Gaussian"}}, 
        Row[{
          Button[
          "Reset", $CellContext`flipg = {0, 1, 1, -1, 
             2}; $CellContext`flipf = {1, -1, 0, 1, 1}; $CellContext`tt = 0; 
           Clear[$CellContext`z]; $CellContext`f$$ = 0; $CellContext`g$$ = 
            0; $CellContext`amp1 = 1; $CellContext`amp2 = 
            1; $CellContext`length1 = 1; $CellContext`length2 = 
            1; $CellContext`delay1 = 0; $CellContext`delay2 = 
            0; $CellContext`period1 = 1; $CellContext`period2 = 
            1; $CellContext`phase1 = -1; $CellContext`phase2 = -1]}], 
        Dynamic[
         Row[{
           Style["f =  ", 20, 
            ColorData[1, 1]], 
           Text[
            ToString[
             $CellContext`f$$[
             Part[$CellContext`flipf, 1] $CellContext`length1 + 
              Part[$CellContext`flipf, 
                 2] ($CellContext`tt - $CellContext`tau)], StandardForm]]}, 
          ImageSize -> {400, 20}]], 
        Column[{
          Row[{
            Style["Amp1", 
             GrayLevel[0]], 
            Slider[
             Dynamic[$CellContext`amp1], {-2, 2}], 
            InputField[
             Dynamic[$CellContext`amp1], ImageSize -> 40]}], 
          Row[{
            Style["Length1", 
             RGBColor[1, 0, 0]], 
            Slider[
             Dynamic[$CellContext`length1], {0.001, 2}], 
            InputField[
             Dynamic[$CellContext`length1], ImageSize -> 50]}], 
          Row[{
            Style["Delay1", 
             RGBColor[0, 0, 1]], 
            Slider[
             Dynamic[$CellContext`delay1], {-2, 2}], 
            InputField[
             Dynamic[$CellContext`delay1], ImageSize -> 40]}], 
          Row[{
            Style["Period1", 
             RGBColor[1, 0.5, 0]], 
            Slider[
             Dynamic[$CellContext`period1], {1.*^-13, 2}], 
            InputField[
             Dynamic[$CellContext`period1], ImageSize -> 55]}], 
          Row[{
            Style["Phase1", 
             RGBColor[0.5, 0, 0.5]], 
            Slider[
             Dynamic[$CellContext`phase1], {-5, 1}], 
            InputField[
             Dynamic[$CellContext`phase1], ImageSize -> 50]}]}], 
        Dynamic[
         Row[{
           Style["g =  ", 20, 
            ColorData[1, 2]], 
           Text[
            ToString[
             $CellContext`g$$[
             Part[$CellContext`flipg, 1] $CellContext`length2 + 
              Part[$CellContext`flipg, 2] $CellContext`z], StandardForm]]}, 
          ImageSize -> {400, 20}]], 
        Row[{
          Style["Amp2", 
           GrayLevel[0]], 
          Slider[
           Dynamic[$CellContext`amp2], {-2, 2}], 
          InputField[
           Dynamic[$CellContext`amp2], ImageSize -> 40]}], 
        Row[{
          Style["Length2", 
           RGBColor[1, 0, 0]], 
          Slider[
           Dynamic[$CellContext`length2], {0.001, 2}], 
          InputField[
           Dynamic[$CellContext`length2], ImageSize -> 50]}], 
        Row[{
          Style["Delay2", 
           RGBColor[0, 0, 1]], 
          Slider[
           Dynamic[$CellContext`delay2], {-2, 2}], 
          InputField[
           Dynamic[$CellContext`delay2], ImageSize -> 40]}], 
        Row[{
          Style["Period2", 
           RGBColor[1, 0.5, 0]], 
          Slider[
           Dynamic[$CellContext`period2], {1.*^-13, 2}], 
          InputField[
           Dynamic[$CellContext`period2], ImageSize -> 55]}], 
        Row[{
          Style["Phase2", 
           RGBColor[0.5, 0, 0.5]], 
          Slider[
           Dynamic[$CellContext`phase2], {-5, 1}], 
          InputField[
           Dynamic[$CellContext`phase2], ImageSize -> 50]}], 
        Row[{
          Button["Set Plot Heights", $CellContext`plotheight = Which[Abs[
                 Max[$CellContext`fdata]] Abs[
                 Max[$CellContext`gdata]] >= Abs[
                Max[$CellContext`fdata]], 1.2 Max[$CellContext`fdata] 
              Max[$CellContext`gdata], Abs[
                 Max[$CellContext`fdata]] Abs[
                 Max[$CellContext`gdata]] >= Abs[
                Max[$CellContext`gdata]], 1.2 Max[$CellContext`fdata] 
              Max[$CellContext`gdata], Abs[
                Max[$CellContext`fdata]] > Abs[
                 Max[$CellContext`fdata]] Abs[
                 Max[$CellContext`gdata]], 1.2 Max[$CellContext`fdata], Abs[
                Max[$CellContext`gdata]] > Abs[
                 Max[$CellContext`fdata]] Abs[
                 Max[$CellContext`gdata]], 1.2 
              Max[$CellContext`gdata]]; $CellContext`convolveheight = 1.2 Max[
               Abs[
               ListConvolve[$CellContext`fdata, $CellContext`gdata, {1, -1}, 
                  0]/100]]]}]}, "Options" :> {}, "DefaultOptions" :> {}],
     ImageSizeCache->{850., {331., 336.}},
     SingleEvaluation->True],
    Deinitialization:>None,
    DynamicModuleValues:>{},
    Initialization:>({$CellContext`fdata = {{{-1., -0.9980267284282716, \
-0.9921147013144778, -0.9822872507286887, -0.9685831611286311, \
-0.9510565162951535, -0.9297764858882513, -0.9048270524660194, \
-0.8763066800438636, -0.8443279255020151, -0.8090169943749473, \
-0.7705132427757891, -0.7289686274214113, -0.6845471059286887, \
-0.6374239897486894, -0.587785252292473, -0.5358267949789964, \
-0.48175367410171505`, -0.4257792915650727, -0.368124552684678, \
-0.30901699437494734`, -0.24868988716485485`, -0.1873813145857246, \
-0.12533323356430415`, -0.0627905195293134, 6.123233995736766*^-17, 
           0.06279051952931353, 0.12533323356430448`, 0.18738131458572493`, 
           0.24868988716485474`, 0.30901699437494745`, 0.3681245526846781, 
           0.4257792915650728, 0.48175367410171555`, 0.535826794978997, 
           0.5877852522924731, 0.6374239897486899, 0.6845471059286888, 
           0.7289686274214114, 0.7705132427757891, 0.8090169943749475, 
           0.8443279255020152, 0.8763066800438635, 0.9048270524660195, 
           0.9297764858882513, 0.9510565162951535, 0.9685831611286312, 
           0.9822872507286887, 0.9921147013144778, 0.9980267284282716, 1., 
           0.9980267284282716, 0.9921147013144778, 0.9822872507286886, 
           0.9685831611286311, 0.9510565162951534, 0.9297764858882512, 
           0.9048270524660195, 0.8763066800438637, 0.8443279255020152, 
           0.8090169943749475, 0.7705132427757891, 0.7289686274214114, 
           0.6845471059286888, 0.6374239897486894, 0.5877852522924731, 
           0.5358267949789962, 0.48175367410171516`, 0.42577929156507205`, 
           0.3681245526846777, 0.30901699437494745`, 0.24868988716485518`, 
           0.18738131458572452`, 0.12533323356430448`, 0.06279051952931308, 
           6.123233995736766*^-17, -0.06279051952931296, \
-0.12533323356430437`, -0.18738131458572438`, -0.24868988716485507`, \
-0.30901699437494734`, -0.36812455268467836`, -0.4257792915650727, \
-0.4817536741017158, -0.5358267949789961, -0.587785252292473, \
-0.6374239897486894, -0.6845471059286887, -0.7289686274214113, \
-0.7705132427757894, -0.8090169943749473, -0.8443279255020153, \
-0.8763066800438636, -0.9048270524660198, -0.9297764858882515, \
-0.9510565162951535, -0.968583161128631, -0.9822872507286887, \
-0.9921147013144778, -0.9980267284282716, 0}}, {{
          0, -0.9980267284282716, -0.9921147013144778, -0.9822872507286887, \
-0.968583161128631, -0.9510565162951535, -0.9297764858882511, \
-0.9048270524660194, -0.8763066800438636, -0.8443279255020153, \
-0.8090169943749473, -0.7705132427757894, -0.7289686274214113, \
-0.6845471059286887, -0.6374239897486894, -0.587785252292473, \
-0.5358267949789961, -0.48175367410171505`, -0.4257792915650727, \
-0.36812455268467836`, -0.30901699437494734`, -0.24868988716485507`, \
-0.18738131458572438`, -0.12533323356430437`, -0.06279051952931296, 
           6.123233995736766*^-17, 0.06279051952931308, 0.12533323356430448`, 
           0.18738131458572452`, 0.24868988716485518`, 0.30901699437494745`, 
           0.3681245526846785, 0.4257792915650728, 0.48175367410171593`, 
           0.535826794978997, 0.5877852522924739, 0.6374239897486894, 
           0.6845471059286888, 0.7289686274214114, 0.7705132427757891, 
           0.8090169943749475, 0.8443279255020152, 0.8763066800438635, 
           0.9048270524660195, 0.9297764858882512, 0.9510565162951534, 
           0.9685831611286311, 0.9822872507286886, 0.9921147013144778, 
           0.9980267284282716, 1., 0.9980267284282716, 0.9921147013144778, 
           0.9822872507286886, 0.9685831611286311, 0.9510565162951534, 
           0.9297764858882512, 0.9048270524660195, 0.8763066800438637, 
           0.8443279255020152, 0.8090169943749475, 0.7705132427757891, 
           0.7289686274214114, 0.6845471059286888, 0.6374239897486899, 
           0.5877852522924731, 0.5358267949789965, 0.48175367410171516`, 
           0.42577929156507244`, 0.3681245526846777, 0.30901699437494706`, 
           0.24868988716485496`, 0.18738131458572493`, 0.12533323356430448`, 
           0.06279051952931353, 
           6.123233995736766*^-17, -0.0627905195293134, \
-0.12533323356430437`, -0.18738131458572482`, -0.24868988716485485`, \
-0.30901699437494756`, -0.3681245526846782, -0.4257792915650727, \
-0.4817536741017158, -0.5358267949789964, -0.587785252292473, \
-0.6374239897486894, -0.6845471059286887, -0.7289686274214113, \
-0.7705132427757894, -0.8090169943749473, -0.8443279255020151, \
-0.8763066800438636, -0.9048270524660196, -0.9297764858882515, \
-0.9510565162951536, -0.968583161128631, -0.9822872507286886, \
-0.9921147013144778, -0.9980267284282716, -1.}}}, $CellContext`flipf = {1, -1,
         0, 1, 1}, $CellContext`length1 = 1, $CellContext`length2 = 
       1, $CellContext`gdata = {{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0}}}, $CellContext`flipg = {0, 1, 1, -1, 2}, 
       Attributes[PlotRange] = {ReadProtected}, $CellContext`convolveheight = 
       1, $CellContext`tt = 0, $CellContext`plotheight = 1, $CellContext`amp1 = 
       1, $CellContext`period1 = 
       1, $CellContext`phase1 = -1, $CellContext`delay1 = 
       0, $CellContext`amp2 = 1, $CellContext`period2 = 
       1, $CellContext`phase2 = -1, $CellContext`delay2 = 0}; 
     Typeset`initDone$$ = True),
    SynchronousInitialization->True,
    UnsavedVariables:>{Typeset`initDone$$},
    UntrackedVariables:>{Typeset`size$$}], "Manipulate",
   Deployed->True,
   StripOnInput->False],
  Manipulate`InterpretManipulate[1]]], "Output",
 CellChangeTimes->{
  3.488750732265625*^9, {3.48875097553125*^9, 3.488751029046875*^9}, {
   3.48875112975*^9, 3.48875117209375*^9}, {3.48875120340625*^9, 
   3.4887512115625*^9}, {3.488752203078125*^9, 3.488752249734375*^9}, {
   3.48875249475*^9, 3.48875253309375*^9}, {3.488753233703125*^9, 
   3.488753245625*^9}, {3.488753512859375*^9, 3.488753531953125*^9}, 
   3.4887537391875*^9, 3.48875416403125*^9}]
},
WindowSize->{1037, 725},
WindowMargins->{{Automatic, 275}, {Automatic, 30}},
DockedCells->FEPrivate`If[
  FEPrivate`SameQ[FEPrivate`$ProductIDName, "MathematicaPlayer"], 
  FEPrivate`Join[{
    Cell[
     BoxData[
      GraphicsBox[
       RasterBox[CompressedData["
1:eJztXXlYU9e27/fe++Pe9757be9tax1AkUGQAMokiLMWB9Q60Dreaq2tA1Zb
LaJ1wjrVjg44KyKCAzIj8zwFSBjCkDCEBAjIKELRKhqSvJ3ssNmckxNGxX7d
v+/od7LPPmuvtWJ+a++19jnqbdy5fON/v/XWWybgz//811tvKc9FA4RQmDVv
AVdnbMpy5+KEBNgm7kT6t3uleuMbxxrVsqbI7T6WTVsrn7paNvPTRs87VUUC
cNREx7e67HlpOVNhYt0x3qJ11LiaS1cGqhIBAQEBwWtHzg0vro5eps5Yjv74
/E9WC27fEeXmivLzM8+eZxtYKsYayMaMe2Y+U+a0S7b8oMz5kHzpfrnjVy+W
7nzh9EWHzbIO8w87TB1emDk817doHG1QFRM71AYREBAQEPQBcPIvSE8vMDQW
6Ojl6+oV6Izj6RrwLOx5do6RJrP4rKkKvTGKMWM7DFjta09Jt17r2HpV+qWH
7LOzslUnpEu+kzp+JbVb9XLS/OesqfW6E+qMLcuzOENtFgEBAQFBHwBjQXlp
abb91OLRY4vG6BeNHc83sMgzss+fuCTefnXTBGtlLNA36NAf92TGCrHbrao9
ng/3eDV/6/lsx+WOLz2kq49L5++UOqxsmzBDomsumTRFxOMNtVkEBAQEBL0F
KgqA87yNm0Sjxlbqm/KMJ2bOcWQvWpa6Ym2881q+48ziD2cK5s8VLFrIn+WQ
vv6LxO2uydt2sbd/m73dNX/rbv7nXwvWbSlyXpuz4COuiW2x9YzygoKhtoyA
gICAoFcQY1B+PORerWPEs5hceuFoTcDl6oBrVUFekkDPOr+Ldfcv1gVcrgu8
Wud/ufb+JUnYrapwH8kDn+oHPjXhPg/DbtWGeD8MvFETdE3sezZ2ynRBarr2
octLSrL9A4TpPXQjICAgIHilAPxf0Ql1MPjukGScde5XX9QG3mgIDquLSJTE
pLeFR/4REvokLKItMrYtJq4tNq4tLKwlKrohh9eQV9CYndfIyW1iZz9OZIP+
1YHhkrDwIo+LkSs3Zoc+KBcK8RFLBQJuYmLU6dMRn2/irFojuXCpoqhoiKwn
+OtCGBIi9PYeai0ICIYYaCEAo0ClCspwIBKVLFsrMZ+Xf9i1NiywKTGnNjWv
IT6Nf/Fic0zis6z8p5m8p5m5Tzh5T9I5TxKS29LYLUWlzcWipkJhU1ZBXSK3
Ijq9PCKlIiGjPDQueJzdzeFjr1ja+i1c4rXc+ecFC/c4TNltPP63d98LGz6i
Zt16SWrqUHuC4K8IEAj4b70FDoGODjh/RaOkD9GCNzw83MXF5eTJk3w+f0gU
IPgTAY8FIApUqSAuLc0/fKzUdK5w5voc971VUQ+q0/IakjJaElNCJk9JsZ/O
v+7VnFPQmlvYkp3fks1rYXPApZa4+OaU1EcpqfVxiVVRCeWhMSWh0WUxifyQ
8O8/GLln2Du/Dnv/0N//8fXf/vf0P4dFvvd+wwjdjgkT6695DrUPCP7SKN28
uWTVKmU4GDasPClp0OXn5ORcuHBh0MX2BiAGBQQE7Nu3z9OT/MoIqBCks1Nu
3ky+dSs3MbEkPx/M/8uFwjI+v4TDKYqN5QcE8c56ZC1emWU4NdPOOWOhC9fd
reJBYH1M0u+xSY/SOckLFj3UMc4bZZC0co0gKLQuO7+Ry6tncxpSMhrjkxoj
ImtDgiVBASJ/v5K7t/N9bhUEB2beu/PV8A/OjxzvM1zHb/jIsrF6ckMjhZ5e
h4VVddiDofYHAYESICKAcFDs4DDokocwFkD4+vqCcDCEChC8mRALBNX3Ajkr
Pz2vb3hk5Ae/WVl62NoGWtukWdlmTrLhWNvzJ08rsbARmjlUWs5usJ7J27e9
+r53U0RMU2Law/Ss2PlO5QbmFaY2pXqmKfoTEnd8LQiPrmJzq5LYkrgkSWS0
JChY7O9fcvdOka9Pzs0bOf5+/Lv3ci1mSEwdpJNmyFl2cpOJCv3xHRYTq6Oj
X6mlQm/vwcoDl+fmKqW9shwCwZADrAhgsmjQawdDHgvA6sDFxWWo8lQEbz4k
Sanir77JMbVINxgfbTSeYz6xzX66YrKDwmKSwshQoT9GYTBOMX5CjpuL+N6t
h5HxkvhUUUp6xDynIiOLIpYNn2VbYmqTO9YkYpJt8vEfBFHx5XEpwvCoMv+g
0jv3Cn198ry9Mjyv8e/fb4xKlq8/IluyQzZtldxmvtzERmZq9TA4uPeqAiqG
v1NwgHPUDhb1Gn+/cI6HDvARXQITP3z6R/kIoL5lzx7UUnb8OBoI9ASX0C3w
nN/50g+gBkUfujRcrFIOi4UPrUxTdBoIExdIeWQUaAfEBRQABwpP4ESgo4Py
3vBeJBwRXdm5c8iloA+0C9xSsnAh1BlqhfxMvxGOArUCfaADgRxgI5AAtaK7
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

        "], {{0, 0}, {516, 41}}, {0, 255}, ColorFunction -> RGBColor], 
       ImageSize -> {516, 41}, PlotRange -> {{0, 516}, {0, 41}}]], 
     "DockedCell", Background -> GrayLevel[0.866682], 
     CellFrame -> {{0, 0}, {0, 4}}, CellFrameColor -> 
     RGBColor[0.690074, 0.12871, 0.194598], CellMargins -> {{0, 0}, {-3, 0}}, 
     CellFrameMargins -> 0, ContextMenu -> None, 
     ComponentwiseContextMenu -> {}], 
    Cell[
     BoxData[
      GridBox[{{
         GraphicsBox[
          RasterBox[CompressedData["
1:eJztWl1Ik1EYFrqNEoLMbHPmNkRrEhJERj93ra4cJvbDRIsyMk1pc03tTL1w
aD+LfkQIJBkR/dBFdtHFDLywCykqoqgLIYRu6jbb8me933e+HT/P2fZN+sZ0
vg9n43zfec973nPe57zvOWNFDS2OhnU5OTlG+LyBj1S/ikAgEAgEAoFAIBAI
xEpFMBh8jUgPYG0z7d7MAOYedm/Bko4Ca5tp92YGSCokle5AUiGpdAeSCkml
O0RS/R1tjEajUIHvyIhdehPyLNZjrVSAgT5CK1Tmf32e+/qcdYEy+/4+vKTd
oQJN89PjojbaHcrCzE8qn0RGfIxrEjcF0SomD49hYqDDUUSGKtUKWRMYz42L
pFJDg1RPamGdlRWGuuDi2Yl+WHkoiy72W6jjgFeSm/wWKEAScCvtCxWpSzxt
1FPU9dJ76Lt8UnEmcVMQrWImUQE6nKJB7sgUKpb4LcpEtHiFpEpEKim2TI8n
IhXv4pAHXCAJEAPIq7d5JFAOYYEGKHEstTbQAJKSx+XH5ZKKE+CmIFq1ZC/E
SBVXoboJyMliKZKKgyapwLlK1ohHKgpYYfGRbnNpg4c8dP3prtckFQ0gUm5K
miKpVZxnRZP4KcSziiU1bjhOobpJ2TtIqnhITipYZ7byydJfoDzMpT9ZWMo1
EHbgBBLyhOWzlmakUtKQnB/VuUYkFbWNjZXIJHEKnFVL0t+IXSQVU4iRKkUk
JxVzhJI7hirpDpXOGzJ5eFLFvMNORxAlaJZhmtmZStQGAvSQA/FECSksH/kt
VLMyokw5qHDOFU3ipiBaxZmtTSo8U2lBI1LF/BWNXZ0Y1LckLo9IqQdYIWdA
9T0uvPT2J2qjrUr2lGMRSzrq4eiIVIxFGC5bMZO4KYhWcWYnSn8gv3j7k+vJ
GbVaSNXt9fo6O/XVib9Tpa+sClLdvtB0q+WSvjqRVGucVA/q6p9V1+irE0m1
xkk1cuLk2O49+upEUq1xUg1X14yZraSrS0ed+H+q9GFV/J+qx25/u9U40Nqa
aUMQ2YMrFRWf8g0vDh9JvUvgYjPR+8KIyA5AyiNeb1Pe5ql840xxSV97eyq9
Hp1y3nE602waYoXC19GRqInIGHC57lY53Lm53/O2RQ3Fr6qqNXUG609/LLX1
ulOiHyL74D/XeLPxvPiexHDtsuulyexbv2GqwLRgNP8xl/W7XAQE1IUQ1nHw
zNl5o2XYoc09RLbC5/E8te26V9+gfskY5SPkuss1abLc2Ljpm9E8Z7FFt++c
POroJoQWn1wotQAPj9XMFZp/m6y9bndGpoNYCZBikbNuoqBwsPY4IxIrQJu+
trYf5pLRfOOXotJw6d7Zsv0LtkOPzzb1ENKjolagueVD5YGodUfEZH2372Cm
p4X4X/wDFcRtOg==
           "], {{0, 0}, {199, 30}}, {0, 255}, ColorFunction -> RGBColor], 
          ImageSize -> {199, 30}, PlotRange -> {{0, 199}, {0, 30}}], 
         ButtonBox[
          GraphicsBox[
           RasterBox[CompressedData["
1:eJztlDFuhFAMRJHS5w45Re6RI+wFcoOUtNtR0lJSUlNSUtNS0m9JXjTKaPQh
UfpgCeT1n2+Px15ebu9vt6eqqp55Xnm+/I/L/p+1bbtt277vvJumIcKbnwWs
73vBsHVdE1bXNQ55Ho8HMOEd3L9tWRZ+6tRBrrjEMAwigHO/37M6SII40zRB
Q7QLkpQWjKOu63RqGHHVIoOYYAR16oucinYGxUo2jqMIzPNMnpQRPF2nMkeS
cCBDXsm7ZNZp6oZRS3wsIBFNIYM2iY8gYmJA8imqawHcOAKqio4KSp4O7Qjp
K1nOUygasaG2hoKSaud3krkArggZzyhhTohP3Uz7E0mNu9g98oj26exOSTon
fRUtn8IUJL+W80gyuz6OW3mgrSXXEv5dSaRTaRz9KQpY/knxtVomKdFQxvFT
JZFR6pFBJWT5tbEvR9+H3F6S+GOSMC+DTHtrHYyh32Qu855cdhn2CUundjY=

            "], {{0, 0}, {55, 14}}, {0, 255}, ColorFunction -> RGBColor], 
           ImageSize -> {55, 14}, PlotRange -> {{0, 55}, {0, 14}}], 
          ButtonData -> {
            URL["http://store.wolfram.com/view/app/playerpro/"], None}, 
          ButtonNote -> "http://store.wolfram.com/view/app/playerpro/"], 
         GraphicsBox[
          
          RasterBox[{{{132, 132, 132}, {156, 155, 155}}, {{138, 137, 137}, {
           171, 169, 169}}, {{138, 137, 137}, {171, 169, 169}}, {{138, 137, 
           137}, {171, 169, 169}}, {{138, 137, 137}, {171, 169, 169}}, {{138, 
           137, 137}, {171, 169, 169}}, {{138, 137, 137}, {171, 169, 169}}, {{
           138, 137, 137}, {171, 169, 169}}, {{138, 137, 137}, {171, 169, 
           169}}, {{138, 137, 137}, {171, 169, 169}}, {{138, 137, 137}, {171, 
           169, 169}}, {{138, 137, 137}, {171, 169, 169}}, {{138, 137, 137}, {
           171, 169, 169}}, {{135, 135, 135}, {167, 166, 166}}}, {{0, 0}, {2, 
           14}}, {0, 255}, ColorFunction -> RGBColor], ImageSize -> {2, 14}, 
          PlotRange -> {{0, 2}, {0, 14}}], 
         ButtonBox[
          GraphicsBox[
           RasterBox[CompressedData["
1:eJztlSGSg2AMhTuzfu+wB1qzR+gF9gbIWhwSW4lEV1ZWYyvxSPZb3vAmDdDq
zpCZdvKH5CUvfwhfx9+f48fhcPjk983vXy922eU9paqqcRx9HGcZhiE+PZ1O
KHVd24KOgt0WFKLQ27ZdJrper0JGcS5CVqs6n89yRpGl73tZBK6kkvv97kCe
ChOlLEvbVW2kiYPoxKcYRdwWld00jS2EcFSFMYXCqbOcBJzL5aJcxK7SFEFB
GQFn8AWupPUkMRfIJEK53W5d1z2hGVP7yohVbfanWoxAJQTfeywbT1+xK9mi
GQGTj8FT0uRAN3Tdjl3SlNANP5WoabIIRP+x52ojLEgXs8fxs7+R1cCXNFH6
SVJhKZwx0+BxBZ7nraGNpBSrEFlA1khgRDcCRg1navIWTeVKPVmliaeaL+c4
tCmcknRHfjtWaS6HNtYmC1wYjGJ+6eKjJcficWgVssz1nGbabFtD682gZaIO
MHXqkq/vyW3GFaQXliNuOhbz9lOHU3a6SrhWkCmnXC9pUoPBt1aQpquYrtJt
0f6Pazkd497W1MkSlxierkrrN254iz8oHqS0ByzxGzfOHx2yq9plYSl8l13e
Xf4ArlmHrg==
            "], {{0, 0}, {77, 14}}, {0, 255}, ColorFunction -> RGBColor], 
           ImageSize -> {77, 14}, PlotRange -> {{0, 77}, {0, 14}}], 
          ButtonData -> {
            URL[
            "http://www.wolfram.com/solutions/interactivedeployment/\
licensingterms.html"], None}, ButtonNote -> 
          "http://www.wolfram.com/solutions/interactivedeployment/\
licensingterms.html"]}}, ColumnsEqual -> False, 
       GridBoxAlignment -> {"Columns" -> {{Center}}, "Rows" -> {{Center}}}]], 
     "DockedCell", Background -> GrayLevel[0.494118], 
     CellFrame -> {{0, 0}, {4, 0}}, CellFrameColor -> 
     RGBColor[0.690074, 0.12871, 0.194598], CellMargins -> 0, 
     CellFrameMargins -> {{0, 0}, {0, -1}}, ContextMenu -> None, 
     ComponentwiseContextMenu -> {}, 
     ButtonBoxOptions -> {ButtonFunction :> (FrontEndExecute[{
          NotebookLocate[#2]}]& ), Appearance -> None, ButtonFrame -> None, 
       Evaluator -> None, Method -> "Queued"}]}, 
   FEPrivate`If[
    FEPrivate`SameQ[
     FrontEnd`CurrentValue[
      FrontEnd`EvaluationNotebook[], ScreenStyleEnvironment], "SlideShow"], {
    Inherited}, {}]], Inherited],
FrontEndVersion->"7.0 for Microsoft Windows (32-bit) (November 10, 2008)",
StyleDefinitions->"Default.nb"
]
(* End of Notebook Content *)

(* Internal cache information *)
(*CellTagsOutline
CellTagsIndex->{}
*)
(*CellTagsIndex
CellTagsIndex->{}
*)
(*NotebookFileOutline
Notebook[{
Cell[545, 20, 27072, 564, 684, "Output"]
}
]
*)

(* End of internal cache information *)
(* NotebookSignature zv0PR9H@tggqqB1wVTgSafwW *)
