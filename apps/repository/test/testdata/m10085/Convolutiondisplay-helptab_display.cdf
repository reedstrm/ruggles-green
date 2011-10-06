(* Content-type: application/vnd.wolfram.cdf.text *)

(*** Wolfram CDF File ***)
(* http://www.wolfram.com/cdf *)

(* CreatedBy='Mathematica 8.0' *)

(*CacheID: 234*)
(* Internal cache information:
NotebookFileLineBreakTest
NotebookFileLineBreakTest
NotebookDataPosition[       150,          7]
NotebookDataLength[     31801,        655]
NotebookOptionsPosition[     31519,        641]
NotebookOutlinePosition[     31877,        657]
CellTagsIndexPosition[     31834,        654]
WindowFrame->Normal*)

(* Beginning of Notebook Content *)
Notebook[{
Cell[BoxData[
 TabViewBox[{{1,"\<\"Lablet\"\>"->
  TagBox[
   StyleBox[
    DynamicModuleBox[{$CellContext`f$$ = $CellContext`amp1 
     Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
      UnitStep[# + $CellContext`delay1] - 
      UnitStep[# + $CellContext`delay1 - $CellContext`length1])& , \
$CellContext`g$$ = $CellContext`amp2 
     Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
      UnitStep[# - $CellContext`delay2] - 
      UnitStep[# - $CellContext`delay2 - $CellContext`length2])& , \
$CellContext`showf$$ = False, $CellContext`showg$$ = 
     False, $CellContext`tt$$ = 0, Typeset`show$$ = True, 
     Typeset`bookmarkList$$ = {}, Typeset`bookmarkMode$$ = "Menu", 
     Typeset`animator$$, Typeset`animvar$$ = 1, Typeset`name$$ = 
     "\"untitled\"", Typeset`specs$$ = {{
       Hold[
        Tooltip[
         Style["\nStart here!", 
          RGBColor[1, 0, 0], 8], 
         "Select signal f and signal g using the pulldown menus. Slide signal \
f across signal g using the t slider below!"]], 
       Manipulate`Dump`ThisIsNotAControl}, {{
        Hold[$CellContext`f$$], $CellContext`amp1 
        Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
         UnitStep[# + $CellContext`delay1] - 
         UnitStep[# + $CellContext`delay1 - $CellContext`length1])& , 
        Style["Select Signal f[t-\[Tau]]", 
         RGBColor[0.24720000000000014`, 0.24, 0.6]]}, {($CellContext`amp1 
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
        Hold[$CellContext`g$$], $CellContext`amp2 
        Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
         UnitStep[# - $CellContext`delay2] - 
         UnitStep[# - $CellContext`delay2 - $CellContext`length2])& , 
        Style["Select Signal g[\[Tau]]", 
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
        "Gaussian"}}, {{
        Hold[$CellContext`tt$$], 0, 
        Style["t", 12]}, -4, 4}, {
       Hold[
        Row[{
          Button[
           Tooltip[
            Style["Reset", 
             RGBColor[1, 0, 0], 12], 
            "Return lablet to initialization settings"], {$CellContext`flipg = \
{0, 1, 1, -1, 2}, $CellContext`flipf = {1, -1, 0, 1, 1}, $CellContext`tt$$ = 
             0; $CellContext`f$$ = $CellContext`amp1 
              Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
               UnitStep[# + $CellContext`delay1] - 
               UnitStep[# + $CellContext`delay1 - $CellContext`length1])& ; \
$CellContext`g$$ = $CellContext`amp2 
              Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
               UnitStep[# - $CellContext`delay2] - 
               UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ; \
$CellContext`amp1 = 1; $CellContext`amp2 = 1; $CellContext`length1 = 
             1; $CellContext`length2 = 1; $CellContext`delay1 = 
             0; $CellContext`delay2 = 0; $CellContext`period1 = 
             1; $CellContext`period2 = 
             1; $CellContext`phase1 = -1; $CellContext`phase2 = -1}]}]], 
       Manipulate`Dump`ThisIsNotAControl}, {
       Hold[
        Dynamic[
         Row[{
           Style["f[t-\[Tau]] =  ", 20, 
            ColorData[1, 1]], 
           If[$CellContext`showf$$, 
            Text[
             $CellContext`f$$[
             Part[$CellContext`flipf, 1] $CellContext`length1 + 
              Part[$CellContext`flipf, 
                 2] ($CellContext`tt$$ - $CellContext`tau)]], Null]}, 
          ImageSize -> {350, 20}]]], Manipulate`Dump`ThisIsNotAControl}, {{
        Hold[$CellContext`showf$$], False, "   Show f[\[Tau]] Formula"}, {
       True, False}}, {
       Hold[
        OpenerView[{
          Tooltip[
           Style["Advanced Setting for f[t-\[Tau]]", 
            RGBColor[1, 0, 0], 8], 
           "Use these advanced settings to change features of the f signal. \
You can change the amplitude, length, delay, and for certain signals, change \
the phase of the signal. To see the results of your slider changes on the \
signal formula, Check the view formula Checkbox."], 
          Column[{
            Manipulate`Place[1], 
            Row[{"flip f ", 
              Checkbox[
               Dynamic[$CellContext`flipf], {{1, -1, 0, 1, 2}, {0, 1, 1, -1, 
               2}}]}], 
            Row[{
              Style["Amp1   ", 
               GrayLevel[0]], 
              Slider[
               Dynamic[$CellContext`amp1], {-2., 2}], "  ", 
              Dynamic[$CellContext`amp1]}], 
            Row[{
              Style["Length1", 
               RGBColor[1, 0, 0]], 
              Slider[
               Dynamic[$CellContext`length1], {0.001, 2}], "  ", 
              Dynamic[$CellContext`length1]}], 
            Row[{
              Style["Delay1 ", 
               RGBColor[0, 0, 1]], 
              Slider[
               Dynamic[$CellContext`delay1], {-2., 2}], "  ", 
              Dynamic[$CellContext`delay1]}], 
            Row[{
              Style["Period1", 
               RGBColor[1, 0.5, 0]], 
              Slider[
               Dynamic[$CellContext`period1], {1.*^-13, 2}], "  ", 
              Dynamic[$CellContext`period1]}], 
            Row[{
              Style["Phase1 ", 
               RGBColor[0.5, 0, 0.5]], 
              Slider[
               Dynamic[$CellContext`phase1], {-5., 1}], "  ", 
              Dynamic[$CellContext`phase1]}]}]}]], 
       Manipulate`Dump`ThisIsNotAControl}, {
       Hold[
        Dynamic[
         Row[{
           Style["g[\[Tau]] =  ", 20, 
            ColorData[1, 2]], 
           If[$CellContext`showg$$, 
            Text[
             $CellContext`g$$[
             Part[$CellContext`flipg, 1] $CellContext`length2 + 
              Part[$CellContext`flipg, 2] $CellContext`z]], Null]}, 
          ImageSize -> {350, 20}]]], Manipulate`Dump`ThisIsNotAControl}, {{
        Hold[$CellContext`showg$$], False, 
        "           Show g[\[Tau]] Formula"}, {True, False}}, {
       Hold[
        OpenerView[{
          Tooltip[
           Style["Advanced Setting for g[\[Tau]]", 
            RGBColor[1, 0, 0], 8], 
           "Use these advanced settings to change features of the g signal. \
You can change the amplitude, length, delay, and for certain signals, change \
the phase of the signal. To see the results of your slider changes on the \
signal formula, Check the view formula Checkbox."], 
          Column[{
            Manipulate`Place[2], 
            Row[{"flip g ", 
              Checkbox[
               Dynamic[$CellContext`flipg], {{0, 1, 1, -1, 1}, {1, -1, 0, 1, 
               1}}]}], 
            Row[{
              Style["Amp2   ", 
               GrayLevel[0]], 
              Slider[
               Dynamic[$CellContext`amp2], {-2., 2}], "  ", 
              Dynamic[$CellContext`amp2]}], 
            Row[{
              Style["Length2", 
               RGBColor[1, 0, 0]], 
              Slider[
               Dynamic[$CellContext`length2], {0.001, 2}], "  ", 
              Dynamic[$CellContext`length2]}], 
            Row[{
              Style["Delay2 ", 
               RGBColor[0, 0, 1]], 
              Slider[
               Dynamic[$CellContext`delay2], {-2., 2}], "  ", 
              Dynamic[$CellContext`delay2]}], 
            Row[{
              Style["Period2", 
               RGBColor[1, 0.5, 0]], 
              Slider[
               Dynamic[$CellContext`period2], {1.*^-13, 2}], "  ", 
              Dynamic[$CellContext`period2]}], 
            Row[{
              Style["Phase2 ", 
               RGBColor[0.5, 0, 0.5]], 
              Slider[
               Dynamic[$CellContext`phase2], {-5., 1}], "  ", 
              Dynamic[$CellContext`phase2]}]}]}]], 
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
     Typeset`size$$ = {360., {210., 214.}}, Typeset`update$$ = 0, 
     Typeset`initDone$$, Typeset`skipInitDone$$ = 
     False, $CellContext`f$10818$$ = False, $CellContext`g$10819$$ = 
     False, $CellContext`tt$10820$$ = 0, $CellContext`showf$10821$$ = 
     False, $CellContext`showg$10822$$ = False}, 
     DynamicBox[Manipulate`ManipulateBoxes[
      2, StandardForm, 
       "Variables" :> {$CellContext`f$$ = $CellContext`amp1 
          Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
           UnitStep[# + $CellContext`delay1] - 
           UnitStep[# + $CellContext`delay1 - $CellContext`length1])& , \
$CellContext`g$$ = $CellContext`amp2 
          Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
           UnitStep[# - $CellContext`delay2] - 
           UnitStep[# - $CellContext`delay2 - $CellContext`length2])& , \
$CellContext`showf$$ = False, $CellContext`showg$$ = False, $CellContext`tt$$ = 
         0}, "ControllerVariables" :> {
         Hold[$CellContext`f$$, $CellContext`f$10818$$, False], 
         Hold[$CellContext`g$$, $CellContext`g$10819$$, False], 
         Hold[$CellContext`tt$$, $CellContext`tt$10820$$, 0], 
         Hold[$CellContext`showf$$, $CellContext`showf$10821$$, False], 
         Hold[$CellContext`showg$$, $CellContext`showg$10822$$, False]}, 
       "OtherVariables" :> {
        Typeset`show$$, Typeset`bookmarkList$$, Typeset`bookmarkMode$$, 
         Typeset`animator$$, Typeset`animvar$$, Typeset`name$$, 
         Typeset`specs$$, Typeset`size$$, Typeset`update$$, 
         Typeset`initDone$$, Typeset`skipInitDone$$}, "Body" :> Dynamic[
         GraphicsColumn[{$CellContext`fdata = {{
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
                  0, $CellContext`length2, 0.01}]}}; Plot[{
              $CellContext`f$$[
              Part[$CellContext`flipf, 1] $CellContext`length1 + 
               Part[$CellContext`flipf, 
                  2] ($CellContext`tt$$ - $CellContext`tau)], 
              $CellContext`g$$[
              Part[$CellContext`flipg, 1] $CellContext`length2 + 
               Part[$CellContext`flipg, 
                  2] $CellContext`tau], $CellContext`f$$[
               Part[$CellContext`flipf, 1] $CellContext`length1 + 
                Part[$CellContext`flipf, 
                   2] ($CellContext`tt$$ - $CellContext`tau)] \
$CellContext`g$$[
               Part[$CellContext`flipg, 1] $CellContext`length2 + 
                Part[$CellContext`flipg, 
                   2] $CellContext`tau]}, {$CellContext`tau, -5, 5}, 
             AxesLabel -> {$CellContext`\[Tau], None}, Epilog -> {{
                ColorData[1, 1], Dashed, 
                Line[{{$CellContext`tt$$, 0}, {$CellContext`tt$$, 0.5}}]}, 
               Text[
                Style["t", 
                 ColorData[1, 1], 12], {$CellContext`tt$$, -0.1}]}, 
             Filling -> {3 -> Axis}, FillingStyle -> LightGray, PlotLabel -> 
             Row[{
                Style["f[t-\[Tau]]", 
                 ColorData[1, 1]], ", ", 
                Style["g[\[Tau]]", 
                 ColorData[1, 2]], ", and ", 
                Style["f[t-\[Tau]]g[\[Tau]]", 
                 ColorData[1, 3]]}], 
             PlotRange -> {-$CellContext`plotheight, \
$CellContext`plotheight}], 
           ListLinePlot[ListConvolve[
              Part[$CellContext`fdata, 
               Part[$CellContext`flipf, 5]], 
              Part[$CellContext`gdata, 
               Part[$CellContext`flipg, 5]], {1, -1}, 0]/100, 
            DataRange -> {0, $CellContext`length2 + $CellContext`length1}, 
            PlotRange -> {-$CellContext`convolveheight, \
$CellContext`convolveheight}, Filling -> Axis, FillingStyle -> Directive[
              Opacity[0.5], Blue], AxesLabel -> {"t", None}, Epilog -> {{
               ColorData[1, 1], Dashed, 
               Line[{{$CellContext`tt$$, 0}, {$CellContext`tt$$, -0.05}}]}, 
              Text[
               Style["   t", 
                ColorData[1, 1], 12], {$CellContext`tt$$, -0.3}]}, PlotLabel -> 
            Row[{
               Style["\[Integral]f[t-\[Tau]]g[\[Tau]]\[DifferentialD]\[Tau]", 
                ColorData[1, 4]]}]]}, ImageSize -> Medium]], "Specifications" :> {
         Tooltip[
          Style["\nStart here!", 
           RGBColor[1, 0, 0], 8], 
          "Select signal f and signal g using the pulldown menus. Slide \
signal f across signal g using the t slider below!"], {{$CellContext`f$$, \
$CellContext`amp1 
           Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
            UnitStep[# + $CellContext`delay1] - 
            UnitStep[# + $CellContext`delay1 - $CellContext`length1])& , 
           Style["Select Signal f[t-\[Tau]]", 
            RGBColor[0.24720000000000014`, 0.24, 0.6]]}, {($CellContext`amp1 
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
             Exp[$CellContext`phase1 #^2] (
              UnitStep[# - 2 $CellContext`delay1] - 
              UnitStep[# - 
               2 ($CellContext`delay1 + $CellContext`length1)])]& ) -> 
           "Gaussian"}}, {{$CellContext`g$$, $CellContext`amp2 
           Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
            UnitStep[# - $CellContext`delay2] - 
            UnitStep[# - $CellContext`delay2 - $CellContext`length2])& , 
           Style["Select Signal g[\[Tau]]", 
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
             Exp[$CellContext`phase2 #^2] (
              UnitStep[# - 2 $CellContext`delay2] - 
              UnitStep[# - 
               2 ($CellContext`delay2 + $CellContext`length2)])]& ) -> 
           "Gaussian"}}, {{$CellContext`tt$$, 0, 
           Style["t", 12]}, -4, 4, Appearance -> "Labeled"}, 
         Row[{
           Button[
            Tooltip[
             Style["Reset", 
              RGBColor[1, 0, 0], 12], 
             "Return lablet to initialization settings"], {$CellContext`flipg = \
{0, 1, 1, -1, 2}, $CellContext`flipf = {1, -1, 0, 1, 1}, $CellContext`tt$$ = 
              0; $CellContext`f$$ = $CellContext`amp1 
               Cos[(2/$CellContext`period1) Pi # + $CellContext`phase1 Pi] (
                UnitStep[# + $CellContext`delay1] - 
                UnitStep[# + $CellContext`delay1 - $CellContext`length1])& ; \
$CellContext`g$$ = $CellContext`amp2 
               Cos[(2/$CellContext`period2) Pi # + $CellContext`phase2 Pi] (
                UnitStep[# - $CellContext`delay2] - 
                UnitStep[# - $CellContext`delay2 - $CellContext`length2])& ; \
$CellContext`amp1 = 1; $CellContext`amp2 = 1; $CellContext`length1 = 
              1; $CellContext`length2 = 1; $CellContext`delay1 = 
              0; $CellContext`delay2 = 0; $CellContext`period1 = 
              1; $CellContext`period2 = 
              1; $CellContext`phase1 = -1; $CellContext`phase2 = -1}]}], 
         Dynamic[
          Row[{
            Style["f[t-\[Tau]] =  ", 20, 
             ColorData[1, 1]], 
            If[$CellContext`showf$$, 
             Text[
              $CellContext`f$$[
              Part[$CellContext`flipf, 1] $CellContext`length1 + 
               Part[$CellContext`flipf, 
                  2] ($CellContext`tt$$ - $CellContext`tau)]], Null]}, 
           ImageSize -> {350, 20}]], {{$CellContext`showf$$, False, 
           "   Show f[\[Tau]] Formula"}, {True, False}, ControlPlacement -> 
          1}, 
         OpenerView[{
           Tooltip[
            Style["Advanced Setting for f[t-\[Tau]]", 
             RGBColor[1, 0, 0], 8], 
            "Use these advanced settings to change features of the f signal. \
You can change the amplitude, length, delay, and for certain signals, change \
the phase of the signal. To see the results of your slider changes on the \
signal formula, Check the view formula Checkbox."], 
           Column[{
             Manipulate`Place[1], 
             Row[{"flip f ", 
               Checkbox[
                Dynamic[$CellContext`flipf], {{1, -1, 0, 1, 2}, {0, 1, 1, -1, 
                2}}]}], 
             Row[{
               Style["Amp1   ", 
                GrayLevel[0]], 
               Slider[
                Dynamic[$CellContext`amp1], {-2., 2}], "  ", 
               Dynamic[$CellContext`amp1]}], 
             Row[{
               Style["Length1", 
                RGBColor[1, 0, 0]], 
               Slider[
                Dynamic[$CellContext`length1], {0.001, 2}], "  ", 
               Dynamic[$CellContext`length1]}], 
             Row[{
               Style["Delay1 ", 
                RGBColor[0, 0, 1]], 
               Slider[
                Dynamic[$CellContext`delay1], {-2., 2}], "  ", 
               Dynamic[$CellContext`delay1]}], 
             Row[{
               Style["Period1", 
                RGBColor[1, 0.5, 0]], 
               Slider[
                Dynamic[$CellContext`period1], {1.*^-13, 2}], "  ", 
               Dynamic[$CellContext`period1]}], 
             Row[{
               Style["Phase1 ", 
                RGBColor[0.5, 0, 0.5]], 
               Slider[
                Dynamic[$CellContext`phase1], {-5., 1}], "  ", 
               Dynamic[$CellContext`phase1]}]}]}], 
         Dynamic[
          Row[{
            Style["g[\[Tau]] =  ", 20, 
             ColorData[1, 2]], 
            If[$CellContext`showg$$, 
             Text[
              $CellContext`g$$[
              Part[$CellContext`flipg, 1] $CellContext`length2 + 
               Part[$CellContext`flipg, 2] $CellContext`z]], Null]}, 
           ImageSize -> {350, 20}]], {{$CellContext`showg$$, False, 
           "           Show g[\[Tau]] Formula"}, {True, False}, 
          ControlPlacement -> 2}, 
         OpenerView[{
           Tooltip[
            Style["Advanced Setting for g[\[Tau]]", 
             RGBColor[1, 0, 0], 8], 
            "Use these advanced settings to change features of the g signal. \
You can change the amplitude, length, delay, and for certain signals, change \
the phase of the signal. To see the results of your slider changes on the \
signal formula, Check the view formula Checkbox."], 
           Column[{
             Manipulate`Place[2], 
             Row[{"flip g ", 
               Checkbox[
                Dynamic[$CellContext`flipg], {{0, 1, 1, -1, 1}, {1, -1, 0, 1, 
                1}}]}], 
             Row[{
               Style["Amp2   ", 
                GrayLevel[0]], 
               Slider[
                Dynamic[$CellContext`amp2], {-2., 2}], "  ", 
               Dynamic[$CellContext`amp2]}], 
             Row[{
               Style["Length2", 
                RGBColor[1, 0, 0]], 
               Slider[
                Dynamic[$CellContext`length2], {0.001, 2}], "  ", 
               Dynamic[$CellContext`length2]}], 
             Row[{
               Style["Delay2 ", 
                RGBColor[0, 0, 1]], 
               Slider[
                Dynamic[$CellContext`delay2], {-2., 2}], "  ", 
               Dynamic[$CellContext`delay2]}], 
             Row[{
               Style["Period2", 
                RGBColor[1, 0.5, 0]], 
               Slider[
                Dynamic[$CellContext`period2], {1.*^-13, 2}], "  ", 
               Dynamic[$CellContext`period2]}], 
             Row[{
               Style["Phase2 ", 
                RGBColor[0.5, 0, 0.5]], 
               Slider[
                Dynamic[$CellContext`phase2], {-5., 1}], "  ", 
               Dynamic[$CellContext`phase2]}]}]}], 
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
                   0]/100]]]}]}, "Options" :> {FrameLabel -> {"", "", 
           Style["Continuous Linear Convolution", Large], ""}, 
         TrackedSymbols :> {$CellContext`amp1, $CellContext`length1, \
$CellContext`delay1, $CellContext`period1, $CellContext`phase1, \
$CellContext`f$$, $CellContext`g$$, $CellContext`amp2, $CellContext`length2, \
$CellContext`delay2, $CellContext`period2, $CellContext`phase2, \
$CellContext`plotheight}, ControlPlacement -> Left, 
         AutorunSequencing -> {1, 4, 5}, Deployed -> True}, 
       "DefaultOptions" :> {}],
      ImageSizeCache->{772., {254., 259.}},
      SingleEvaluation->True],
     Deinitialization:>None,
     DynamicModuleValues:>{},
     Initialization:>({$CellContext`functionlist1 = {($CellContext`amp1 
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
             Exp[$CellContext`phase1 #^2] (
              UnitStep[# - 2 $CellContext`delay1] - 
              UnitStep[# - 
               2 ($CellContext`delay1 + $CellContext`length1)])]& ) -> 
           "Gaussian"}; $CellContext`functionlist2 = {($CellContext`amp2 
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
             Exp[$CellContext`phase2 #^2] (
              UnitStep[# - 2 $CellContext`delay2] - 
              UnitStep[# - 
               2 ($CellContext`delay2 + $CellContext`length2)])]& ) -> 
           "Gaussian"}; $CellContext`plotheight = $CellContext`amp1; \
$CellContext`convolveheight = $CellContext`amp1; $CellContext`amp1 = 
         1; $CellContext`tt$$ = 0; $CellContext`amp1 = 1; $CellContext`amp2 = 
         1; $CellContext`length1 = 1; $CellContext`length2 = 
         1; $CellContext`delay1 = 0; $CellContext`delay2 = 
         0; $CellContext`period1 = 1; $CellContext`period2 = 
         1; $CellContext`phase1 = -1; $CellContext`phase2 = -1; \
$CellContext`flipg = {0, 1, 1, -1, 2}; $CellContext`flipf = {1, -1, 0, 1, 1}; 
        Null}; Typeset`initDone$$ = True),
     SynchronousInitialization->True,
     UnsavedVariables:>{Typeset`initDone$$},
     UntrackedVariables:>{Typeset`size$$}], "Manipulate",
    Deployed->True,
    StripOnInput->False],
   Manipulate`InterpretManipulate[1]]}, {
  2,"\<\"Help\"\>"->"\<\"This Lablet demonstrates the principles of \
Convolution applied to two zero-padded signals. Play with the top slider, T, \
to change the position of the f[\[Tau]] signal. The next set of sliders \
controls the type of signal to be convolved. The remaining box of sliders \
controls more advanced features of the signal.\"\>"}}, 
  1]], "PluginEmbeddedContent",
 CellChangeTimes->{
  3.522166019247756*^9, {3.5221660597166348`*^9, 3.522166085172312*^9}, {
   3.522166404091041*^9, 3.522166413505686*^9}, {3.522782400409157*^9, 
   3.522782406803808*^9}, 3.522782456639523*^9, 3.522782533790539*^9, 
   3.522782606665942*^9, 3.522782646022717*^9, {3.522782937810512*^9, 
   3.5227829525688562`*^9}, {3.5227830207816677`*^9, 3.522783036117667*^9}, 
   3.522783076190528*^9, {3.522783161777906*^9, 3.5227832044008217`*^9}}]
},
WindowSize->{1127, 743},
WindowMargins->{{Automatic, 17}, {-72, Automatic}},
FrontEndVersion->"8.0 for Mac OS X x86 (32-bit, 64-bit Kernel) (November 6, \
2010)",
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
Cell[550, 20, 30965, 619, 566, "PluginEmbeddedContent"]
}
]
*)

(* End of internal cache information *)

(* NotebookSignature 2uDsjtdGhnpi4BKYbN06nKbf *)
