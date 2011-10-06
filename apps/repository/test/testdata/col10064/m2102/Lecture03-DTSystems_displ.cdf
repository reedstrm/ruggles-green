(* Content-type: application/vnd.wolfram.cdf.text *)

(*** Wolfram CDF File ***)
(* http://www.wolfram.com/cdf *)

(* CreatedBy='Mathematica 8.0' *)

(*CacheID: 234*)
(* Internal cache information:
NotebookFileLineBreakTest
NotebookFileLineBreakTest
NotebookDataPosition[       150,          7]
NotebookDataLength[     30153,        665]
NotebookOptionsPosition[     29813,        648]
NotebookOutlinePosition[     30171,        664]
CellTagsIndexPosition[     30128,        661]
WindowFrame->Normal*)

(* Beginning of Notebook Content *)
Notebook[{

Cell[CellGroupData[{
Cell[BoxData[
 RowBox[{"Manipulate", "[", "\[IndentingNewLine]", 
  RowBox[{
   RowBox[{"GraphicsRow", "[", 
    RowBox[{"{", "\[IndentingNewLine]", 
     RowBox[{
      RowBox[{"Plot", "[", 
       RowBox[{
        RowBox[{"sys", "[", 
         RowBox[{"a", ",", "t", ",", "p"}], "]"}], ",", 
        RowBox[{"{", 
         RowBox[{"t", ",", "0", ",", "10"}], "}"}], ",", 
        RowBox[{"ImageSize", "\[Rule]", "S"}], ",", 
        RowBox[{"PlotStyle", "\[Rule]", "Thick"}], ",", 
        RowBox[{"PlotLabel", "\[Rule]", " ", 
         RowBox[{"Dynamic", "[", 
          RowBox[{"If", "[", 
           RowBox[{"showInput", ",", 
            RowBox[{"sys", "[", 
             RowBox[{"a", ",", "t", ",", "p"}], "]"}], ",", 
            "\"\<\[CapitalPsi][t]\>\""}], "]"}], "]"}]}], ",", 
        RowBox[{"PlotRange", "->", 
         RowBox[{"{", 
          RowBox[{
           RowBox[{"{", 
            RowBox[{"0", ",", "10"}], "}"}], ",", 
           RowBox[{"{", 
            RowBox[{
             RowBox[{"1.2", " ", "*", " ", 
              RowBox[{"-", "6"}]}], ",", 
             RowBox[{"1.2", "*", "6"}]}], "}"}]}], "}"}]}]}], "]"}], ",", 
      "\[IndentingNewLine]", 
      RowBox[{"Dynamic", "[", 
       RowBox[{
        RowBox[{"sysequation", " ", "=", " ", 
         RowBox[{"{", 
          RowBox[{
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", "g", ",", "p"}], "]"}], ",", 
           SuperscriptBox[
            RowBox[{"(", 
             RowBox[{"sys", "[", 
              RowBox[{"a", ",", "g", ",", "p"}], "]"}], ")"}], "2"], ",", 
           RowBox[{"sys", "[", 
            RowBox[{
             RowBox[{"2", "a"}], ",", "g", ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", 
             SuperscriptBox["g", "2"], ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", 
             RowBox[{"2", "g"}], ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{
             RowBox[{"g", " ", "a"}], ",", "g", ",", "p"}], "]"}], ",", 
           RowBox[{"Piecewise", "[", 
            RowBox[{"{", 
             RowBox[{
              RowBox[{"{", 
               RowBox[{
                RowBox[{"sys", "[", 
                 RowBox[{"a", ",", "g", ",", "p"}], "]"}], ",", 
                RowBox[{
                 RowBox[{"sys", "[", 
                  RowBox[{"a", ",", "g", ",", "p"}], "]"}], "<", "3"}]}], 
               "}"}], ",", 
              RowBox[{"{", 
               RowBox[{"3", ",", 
                RowBox[{
                 RowBox[{"sys", "[", 
                  RowBox[{"a", ",", "g", ",", "p"}], "]"}], ">", "3"}]}], 
               "}"}]}], "}"}], "]"}], 
           RowBox[{"(*", 
            RowBox[{",", 
             RowBox[{"Evaluate", "[", 
              RowBox[{"D", "[", 
               RowBox[{
                RowBox[{"sys", "[", 
                 RowBox[{"a", ",", "g", ",", "p"}], "]"}], ",", "g"}], "]"}], 
              "]"}], ",", 
             RowBox[{"Integrate", "[", 
              RowBox[{
               RowBox[{"sys", "[", 
                RowBox[{"a", ",", "u", ",", "p"}], "]"}], ",", 
               RowBox[{"{", 
                RowBox[{"u", ",", 
                 RowBox[{"z", "-", "1"}], ",", "z"}], "}"}]}], "]"}]}], 
            "*)"}], ",", 
           RowBox[{"(", 
            RowBox[{
             RowBox[{"sys", "[", 
              RowBox[{"a", ",", "g", ",", "p"}], "]"}], "+", "1"}], ")"}]}], 
          "}"}]}], ";", "\[IndentingNewLine]", 
        RowBox[{"If", "[", 
         RowBox[{"showPlot", ",", 
          RowBox[{"Plot", "[", 
           RowBox[{
            RowBox[{"sysequation", "[", 
             RowBox[{"[", "system", "]"}], "]"}], ",", 
            RowBox[{"{", 
             RowBox[{"g", ",", "0", ",", "10"}], "}"}], ",", 
            RowBox[{"ImageSize", "\[Rule]", "S"}], ",", 
            RowBox[{"PlotStyle", "\[Rule]", "Thick"}], ",", 
            RowBox[{"PlotLabel", "\[Rule]", 
             RowBox[{"Dynamic", "[", 
              RowBox[{"If", "[", 
               RowBox[{"showOutput", ",", 
                RowBox[{"sysequation", "[", 
                 RowBox[{"[", "system", "]"}], "]"}], ",", 
                "\"\<\[CapitalPhi][t]\>\""}], "]"}], "]"}]}], ",", 
            RowBox[{"PlotRange", "->", 
             RowBox[{"{", 
              RowBox[{
               RowBox[{"{", 
                RowBox[{"0", ",", "10"}], "}"}], ",", 
               RowBox[{"{", 
                RowBox[{
                 RowBox[{
                  RowBox[{"-", "1.2"}], " ", "*", "6"}], ",", 
                 RowBox[{"1.2", "*", "6"}]}], "}"}]}], "}"}]}]}], "]"}], ",", 
          RowBox[{"Dynamic", "[", 
           RowBox[{"If", "[", 
            RowBox[{"showOutput", ",", 
             RowBox[{"sysequation", "[", 
              RowBox[{"[", "system", "]"}], "]"}], ",", 
             "\"\<\[CapitalPhi][t]\>\""}], "]"}], "]"}]}], "]"}]}], "]"}]}], 
     "\[IndentingNewLine]", "}"}], "]"}], ",", "\[IndentingNewLine]", 
   RowBox[{"Tooltip", "[", 
    RowBox[{
     RowBox[{"Style", "[", 
      RowBox[{"\"\<\nStart here!\>\"", ",", "Red", ",", "8"}], "]"}], ",", 
     "\"\<This module tests your ability to determine which systems applied \
to a signal are Linear and Time-Invariant. To help you determine, Slide \
Amplitude and Position sliders and observe the effect on the output signal \
\[CapitalPhi][t]. You can also change the type of signal you observe with the \
input Signal Pulldown Menu. Try and guess which of the Systems are Linear \
Time Invariant!\>\""}], "]"}], ",", "\[IndentingNewLine]", 
   RowBox[{"Row", "[", 
    RowBox[{"{", 
     RowBox[{
      RowBox[{"Style", "[", 
       RowBox[{"\"\<Input Amplitude\>\"", ",", " ", "Black"}], "]"}], ",", 
      RowBox[{"Slider", "[", 
       RowBox[{
        RowBox[{"Dynamic", "[", "a", "]"}], ",", 
        RowBox[{"{", 
         RowBox[{"0", ",", "5"}], "}"}], ",", 
        RowBox[{"Appearance", "\[Rule]", "\"\<Labeled\>\""}]}], "]"}]}], 
     RowBox[{"(*", 
      RowBox[{",", 
       RowBox[{"InputField", "[", 
        RowBox[{
         RowBox[{"Dynamic", "[", "a", "]"}], ",", "Number", ",", 
         RowBox[{"ImageSize", "\[Rule]", "40"}]}], "]"}]}], "*)"}], "}"}], 
    "]"}], ",", "\[IndentingNewLine]", 
   RowBox[{"Row", "[", 
    RowBox[{"{", 
     RowBox[{
      RowBox[{"Style", "[", 
       RowBox[{"\"\<Input Position   \>\"", ",", " ", "Black"}], "]"}], ",", 
      RowBox[{"Slider", "[", 
       RowBox[{
        RowBox[{"Dynamic", "[", "p", "]"}], ",", 
        RowBox[{"{", 
         RowBox[{"0", ",", "5"}], "}"}], ",", 
        RowBox[{"Appearance", "\[Rule]", "\"\<Labeled\>\""}]}], "]"}]}], 
     RowBox[{"(*", 
      RowBox[{",", 
       RowBox[{"InputField", "[", 
        RowBox[{
         RowBox[{"Dynamic", "[", "p", "]"}], ",", "Number", ",", 
         RowBox[{"ImageSize", "\[Rule]", "40"}]}], "]"}]}], "*)"}], "}"}], 
    "]"}], ",", 
   RowBox[{"{", 
    RowBox[{
     RowBox[{"{", 
      RowBox[{"fins", ",", "UnitBox", ",", "\"\<Input signal   \>\""}], "}"}],
      ",", 
     RowBox[{"{", 
      RowBox[{
       RowBox[{"UnitBox", "\[Rule]", "\"\<Pulse\>\""}], ",", 
       RowBox[{"UnitStep", "\[Rule]", "\"\<Step\>\""}], ",", 
       RowBox[{"SawtoothWave", "\[Rule]", "\"\<Saw\>\""}], ",", " ", 
       RowBox[{"Sin", " ", "\[Rule]", " ", "\"\<Sine\>\""}], ",", 
       RowBox[{"F1", "\[Rule]", "\"\<F1\>\""}], ",", 
       RowBox[{"Ex", "\[Rule]", "\"\<Exp\>\""}]}], "}"}]}], "}"}], ",", 
   "\[IndentingNewLine]", 
   RowBox[{"{", 
    RowBox[{
     RowBox[{"{", 
      RowBox[{"system", ",", "1", ",", "\"\<Systems   \>\""}], "}"}], ",", 
     RowBox[{"{", 
      RowBox[{
       RowBox[{"1", "\[Rule]", "\"\<1\>\""}], ",", 
       RowBox[{"2", "\[Rule]", "\"\<2\>\""}], ",", 
       RowBox[{"3", "\[Rule]", "\"\<3\>\""}], ",", " ", 
       RowBox[{"4", "\[Rule]", "\"\<4\>\""}], ",", " ", 
       RowBox[{"5", "\[Rule]", "\"\<5\>\""}], ",", " ", 
       RowBox[{"6", "\[Rule]", "\"\<6\>\""}], ",", 
       RowBox[{"7", "\[Rule]", "\"\<7\>\""}], ",", 
       RowBox[{"8", "\[Rule]", "\"\<8\>\""}]}], 
      RowBox[{"(*", 
       RowBox[{",", 
        RowBox[{"9", "\[Rule]", "\"\<9\>\""}], ",", 
        RowBox[{"10", "\[Rule]", "\"\<10\>\""}]}], "*)"}], "}"}]}], "}"}], 
   ",", "\[IndentingNewLine]", 
   RowBox[{"Row", "[", 
    RowBox[{"{", "\[IndentingNewLine]", 
     RowBox[{
      RowBox[{"Control", "[", 
       RowBox[{"{", 
        RowBox[{
         RowBox[{"{", 
          RowBox[{"showInput", ",", "False", ",", "\"\<Show Input Eq\>\""}], 
          "}"}], ",", 
         RowBox[{"{", 
          RowBox[{"True", ",", "False"}], "}"}]}], "}"}], "]"}], ",", 
      "\[IndentingNewLine]", 
      RowBox[{"Control", "[", 
       RowBox[{"{", 
        RowBox[{
         RowBox[{"{", 
          RowBox[{
          "showOutput", ",", "False", ",", 
           "\"\<           Show Answer Eq\>\""}], "}"}], ",", 
         RowBox[{"{", 
          RowBox[{"True", ",", "False"}], "}"}]}], "}"}], "]"}], ",", 
      RowBox[{"Control", "[", 
       RowBox[{"{", 
        RowBox[{
         RowBox[{"{", 
          RowBox[{
          "showPlot", ",", "True", ",", "\"\<         Show Answer Plot\>\""}],
           "}"}], ",", 
         RowBox[{"{", 
          RowBox[{"True", ",", "False"}], "}"}]}], "}"}], "]"}]}], "}"}], 
    "]"}], ",", "\[IndentingNewLine]", 
   RowBox[{"FrameLabel", "\[Rule]", 
    RowBox[{"{", 
     RowBox[{"\"\<\>\"", ",", "\"\<\>\"", ",", 
      RowBox[{"Style", "[", 
       RowBox[{"\"\<LTI Drill\>\"", ",", "Large"}], "]"}], ",", "\"\<\>\""}], 
     "}"}]}], ",", "\[IndentingNewLine]", 
   RowBox[{"Initialization", "\[RuleDelayed]", "\[IndentingNewLine]", 
    RowBox[{"{", 
     RowBox[{
      RowBox[{"a", "=", "1"}], ";", " ", 
      RowBox[{"p", "=", "0"}], ";", 
      RowBox[{"S", "=", 
       RowBox[{"{", 
        RowBox[{"300", ",", "200"}], "}"}]}], ";", "\[IndentingNewLine]", 
      RowBox[{"(*", 
       RowBox[{
        RowBox[{"sysequation", " ", "=", " ", 
         RowBox[{"{", 
          RowBox[{
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", "t", ",", "p"}], "]"}], ",", 
           SuperscriptBox[
            RowBox[{"(", 
             RowBox[{"sys", "[", 
              RowBox[{"a", ",", "t", ",", "p"}], "]"}], ")"}], "2"], ",", 
           RowBox[{"sys", "[", 
            RowBox[{
             RowBox[{"2", "a"}], ",", "t", ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", 
             SuperscriptBox["t", "2"], ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{"a", ",", 
             RowBox[{"2", "t"}], ",", "p"}], "]"}], ",", 
           RowBox[{"sys", "[", 
            RowBox[{
             RowBox[{"t", " ", "a"}], ",", "t", ",", "p"}], "]"}], ",", 
           RowBox[{"Piecewise", "[", 
            RowBox[{"{", 
             RowBox[{
              RowBox[{"{", 
               RowBox[{
                RowBox[{"sys", "[", 
                 RowBox[{"a", ",", "t", ",", "p"}], "]"}], ",", 
                RowBox[{
                 RowBox[{"sys", "[", 
                  RowBox[{"a", ",", "t", ",", "p"}], "]"}], "<", "3"}]}], 
               "}"}], ",", 
              RowBox[{"{", 
               RowBox[{"3", ",", 
                RowBox[{
                 RowBox[{"sys", "[", 
                  RowBox[{"a", ",", "t", ",", "p"}], "]"}], ">", "3"}]}], 
               "}"}]}], "}"}], "]"}], ",", 
           RowBox[{"(*", 
            RowBox[{
             RowBox[{"Evaluate", "[", 
              RowBox[{"D", "[", 
               RowBox[{
                RowBox[{"sys", "[", 
                 RowBox[{"a", ",", "t", ",", "p"}], "]"}], ",", "t"}], "]"}], 
              "]"}], ",", 
             RowBox[{"Integrate", "[", 
              RowBox[{
               RowBox[{"sys", "[", 
                RowBox[{"a", ",", "u", ",", "p"}], "]"}], ",", 
               RowBox[{"{", 
                RowBox[{"u", ",", 
                 RowBox[{"z", "-", "1"}], ",", "z"}], "}"}]}], "]"}], ","}], 
            "*)"}], 
           RowBox[{"(", 
            RowBox[{
             RowBox[{"sys", "[", 
              RowBox[{"a", ",", "t", ",", "p"}], "]"}], "-", "1"}], ")"}]}], 
          "}"}]}], ";"}], "*)"}], "\[IndentingNewLine]", 
      RowBox[{
       RowBox[{"sys", "[", 
        RowBox[{"az_", ",", "tz_", ",", "pz_"}], "]"}], " ", ":=", " ", 
       RowBox[{"az", " ", 
        RowBox[{"fins", "[", 
         RowBox[{"tz", "-", "pz"}], "]"}]}]}], ";", "\[IndentingNewLine]", 
      RowBox[{
       RowBox[{"F1", "[", "tz_", "]"}], ":=", 
       RowBox[{"Piecewise", "[", 
        RowBox[{"{", 
         RowBox[{
          RowBox[{"{", 
           RowBox[{
            SuperscriptBox["tz", "2"], ",", 
            RowBox[{"0", "<", "tz", "<", "2"}]}], "}"}], ",", 
          RowBox[{"{", 
           RowBox[{"0", ",", 
            RowBox[{"tz", "\[LessEqual]", "0"}]}], " ", "}"}], ",", 
          RowBox[{"{", 
           RowBox[{
            RowBox[{
             RowBox[{"-", "tz"}], "+", "4"}], ",", 
            RowBox[{"2", "\[LessEqual]", "tz", "<", "3"}]}], "}"}], ",", 
          RowBox[{"{", 
           RowBox[{"0", ",", 
            RowBox[{"tz", "\[GreaterEqual]", " ", "3"}]}], "}"}]}], "}"}], 
        "]"}]}], ";", "\[IndentingNewLine]", 
      RowBox[{
       RowBox[{"Ex", "[", "tz_", "]"}], ":=", 
       RowBox[{"Exp", "[", 
        RowBox[{".5", "tz"}], " ", "]"}]}], ";"}], "\[IndentingNewLine]", 
     "}"}]}], ",", 
   RowBox[{"LocalizeVariables", "\[Rule]", "True"}], ",", 
   RowBox[{"Deployed", "\[Rule]", "True"}]}], "]"}]], "Input",
 CellChangeTimes->{{3.507922779140625*^9, 3.507922910640625*^9}, {
   3.50792295596875*^9, 3.507923227984375*^9}, {3.507923281359375*^9, 
   3.50792330396875*^9}, {3.507988417484375*^9, 3.5079884284375*^9}, {
   3.50798862325*^9, 3.50798867859375*^9}, {3.507988805265625*^9, 
   3.5079888153125*^9}, {3.507989022609375*^9, 3.507989046140625*^9}, {
   3.50808383375*^9, 3.508083935625*^9}, {3.5080840099375*^9, 
   3.50808424078125*^9}, {3.508084274671875*^9, 3.508084351953125*^9}, {
   3.508084399578125*^9, 3.50808440378125*^9}, {3.508084915546875*^9, 
   3.508084964875*^9}, {3.508085053390625*^9, 3.50808507234375*^9}, {
   3.5080861208125*^9, 3.508086135796875*^9}, {3.508086168625*^9, 
   3.508086256765625*^9}, {3.508086474875*^9, 3.508086479234375*^9}, {
   3.5080865249375*^9, 3.50808654421875*^9}, {3.50808664278125*^9, 
   3.5080866955*^9}, {3.508086742140625*^9, 3.5080867424375*^9}, {
   3.508086787421875*^9, 3.5080868396875*^9}, {3.508087309046875*^9, 
   3.50808733825*^9}, {3.5080873920625*^9, 3.50808748515625*^9}, {
   3.5080875939375*^9, 3.508087598140625*^9}, {3.5080876619375*^9, 
   3.508087664859375*^9}, {3.50808771021875*^9, 3.508087714046875*^9}, {
   3.508087789140625*^9, 3.508087798921875*^9}, {3.508087915703125*^9, 
   3.50808791878125*^9}, {3.508087976375*^9, 3.508088034265625*^9}, {
   3.508090205171875*^9, 3.508090211515625*^9}, {3.508090301625*^9, 
   3.508090304609375*^9}, {3.508090728359375*^9, 3.5080907434375*^9}, {
   3.5080909073125*^9, 3.508091037703125*^9}, {3.508160114625*^9, 
   3.508160136*^9}, {3.50816019021875*^9, 3.508160283984375*^9}, {
   3.508160336609375*^9, 3.5081603413125*^9}, {3.508160670078125*^9, 
   3.508160675390625*^9}, {3.50816323315625*^9, 3.508163237015625*^9}, {
   3.50816331090625*^9, 3.508163328328125*^9}, {3.5105020066875*^9, 
   3.51050202265625*^9}, {3.5105023038125*^9, 3.510502341703125*^9}, {
   3.510502385390625*^9, 3.510502458203125*^9}, {3.510502490703125*^9, 
   3.510502494640625*^9}, {3.510502633578125*^9, 3.51050276746875*^9}, {
   3.51050285271875*^9, 3.510502906359375*^9}, {3.51050301871875*^9, 
   3.51050306178125*^9}, {3.510503108546875*^9, 3.51050311*^9}, {
   3.510503149484375*^9, 3.51050316128125*^9}, {3.5105032143125*^9, 
   3.510503301390625*^9}, 3.5105033685*^9, 3.510503456671875*^9, {
   3.510589944114375*^9, 3.51058998747375*^9}, {3.516993798875*^9, 
   3.516993829609375*^9}, {3.51699399075*^9, 3.516994020078125*^9}, {
   3.516994088484375*^9, 3.5169940890625*^9}, {3.5202589271330233`*^9, 
   3.520258933518712*^9}, {3.520259081431162*^9, 3.520259115972373*^9}, {
   3.5202591660682077`*^9, 3.520259223691486*^9}, {3.520259299651128*^9, 
   3.520259361449711*^9}, {3.52025940273142*^9, 3.520259402956785*^9}, {
   3.5202594338431883`*^9, 3.52025944199851*^9}, {3.520259491498217*^9, 
   3.5202595254227*^9}, {3.5202596439229717`*^9, 3.520259719176354*^9}, {
   3.5202598072984247`*^9, 3.520259814718485*^9}, {3.520259849277952*^9, 
   3.52025992543674*^9}, {3.520260028987561*^9, 3.520260053749683*^9}, {
   3.520260109549604*^9, 3.5202601300460653`*^9}, {3.5202601955596933`*^9, 
   3.520260220395837*^9}, {3.520260264453412*^9, 3.520260283314001*^9}, {
   3.520260416090377*^9, 3.520260424767353*^9}, {3.520260488229903*^9, 
   3.520260598611105*^9}, 3.520260629116171*^9, {3.5202606598311043`*^9, 
   3.520260724063958*^9}, {3.520260776937564*^9, 3.520260780168441*^9}, {
   3.520260885009729*^9, 3.5202608857127542`*^9}, {3.5202609214465446`*^9, 
   3.5202609234496202`*^9}, {3.520261037022118*^9, 3.520261130467263*^9}, {
   3.520261168045845*^9, 3.520261179629078*^9}, {3.520261230177774*^9, 
   3.5202612864815903`*^9}, {3.520261322825946*^9, 3.52026140356332*^9}, {
   3.520261496598866*^9, 3.5202615994761553`*^9}, {3.520261833465988*^9, 
   3.5202619412362823`*^9}, {3.520262048949367*^9, 3.520262101502931*^9}, {
   3.520262176902281*^9, 3.520262177916192*^9}, {3.52026228116293*^9, 
   3.520262284889944*^9}, {3.520262579435734*^9, 3.520262645337661*^9}, {
   3.520262683909293*^9, 3.520262689968099*^9}, {3.520262754867031*^9, 
   3.5202627906738653`*^9}, {3.520262838904866*^9, 3.520262858570156*^9}, {
   3.520262947250163*^9, 3.5202629480609818`*^9}, {3.5202629934154377`*^9, 
   3.5202630035562897`*^9}, {3.52026312602791*^9, 3.520263139154998*^9}, {
   3.520263190827745*^9, 3.520263234572401*^9}, {3.520263265663043*^9, 
   3.520263269648551*^9}, {3.520263299773426*^9, 3.520263300370953*^9}, {
   3.520263389289352*^9, 3.5202633895106487`*^9}, {3.520263483676982*^9, 
   3.5202635159179373`*^9}, {3.5202635494762506`*^9, 
   3.5202635661824427`*^9}, {3.520263850122929*^9, 3.520263851833199*^9}, {
   3.520263889744763*^9, 3.5202638901588507`*^9}, {3.520264048238022*^9, 
   3.520264118923875*^9}, {3.520264275425632*^9, 3.5202643034287577`*^9}, {
   3.5202643512332687`*^9, 3.520264353015633*^9}, {3.520264386275481*^9, 
   3.520264388327643*^9}, 3.520264462603108*^9, {3.520264561891478*^9, 
   3.5202645671856413`*^9}, {3.520264621986047*^9, 3.5202646869319277`*^9}, {
   3.520264774551724*^9, 3.5202647855202217`*^9}, {3.5202648380556803`*^9, 
   3.520264873052985*^9}, {3.520264921278344*^9, 3.520264943466774*^9}, {
   3.520265028155191*^9, 3.5202650321701593`*^9}, 3.520265068985085*^9, {
   3.520265104700727*^9, 3.520265248746893*^9}, {3.520265292251766*^9, 
   3.5202653193532143`*^9}, 3.5202653614190683`*^9, {3.520793321912623*^9, 
   3.520793325529468*^9}, {3.520793356404242*^9, 3.5207933966023293`*^9}, {
   3.520793640001253*^9, 3.520793668085784*^9}, {3.520795636116125*^9, 
   3.520795642115026*^9}, {3.5208561561166677`*^9, 3.5208561652140417`*^9}, {
   3.520856640783143*^9, 3.520856653435807*^9}, 3.520857412455558*^9, {
   3.52085784442969*^9, 3.520857845684237*^9}, {3.520867557664671*^9, 
   3.5208675579262133`*^9}, {3.5208676391547937`*^9, 
   3.5208676432465973`*^9}, {3.520867761891078*^9, 3.520867763632082*^9}, {
   3.520869586973434*^9, 3.520869589803259*^9}, {3.520869797904935*^9, 
   3.520869801792653*^9}, {3.520869846240818*^9, 3.52086984702239*^9}, {
   3.520869968264348*^9, 3.520870012388623*^9}, {3.520870066121072*^9, 
   3.520870123036503*^9}, {3.520870159724801*^9, 3.520870223163189*^9}, 
   3.5208704210249977`*^9, 3.520870484646215*^9, {3.5208705188078527`*^9, 
   3.520870558298675*^9}, {3.520870601457582*^9, 3.520870629556332*^9}, {
   3.520965757441839*^9, 3.520965757705531*^9}, {3.521225073542677*^9, 
   3.5212251094282427`*^9}, {3.521229465535804*^9, 3.521229465925879*^9}, {
   3.521496187413817*^9, 3.521496190695977*^9}, {3.521496220837192*^9, 
   3.521496225170182*^9}, {3.521496255588254*^9, 3.5214962580331907`*^9}, 
   3.5220065690955753`*^9, 3.522184260350065*^9, 3.5221845197272587`*^9, {
   3.5224394077160873`*^9, 3.522439416362903*^9}, {3.522439550766059*^9, 
   3.522439551187355*^9}, {3.522781285732573*^9, 3.522781433155402*^9}, {
   3.5227814893307543`*^9, 3.522781491293277*^9}, {3.522781537410028*^9, 
   3.522781586144088*^9}}],

Cell[BoxData[
 TagBox[
  StyleBox[
   DynamicModuleBox[{$CellContext`fins$$ = UnitBox, $CellContext`showInput$$ =
     False, $CellContext`showOutput$$ = False, $CellContext`showPlot$$ = 
    True, $CellContext`system$$ = 1, Typeset`show$$ = True, 
    Typeset`bookmarkList$$ = {}, Typeset`bookmarkMode$$ = "Menu", 
    Typeset`animator$$, Typeset`animvar$$ = 1, Typeset`name$$ = 
    "\"untitled\"", Typeset`specs$$ = {{
      Hold[
       Tooltip[
        Style["\nStart here!", 
         RGBColor[1, 0, 0], 8], 
        "Slide A (amplitude) and \!\(\*SubscriptBox[\(t\), \(0\)]\) and \
observe the effect. What do you think each slider does? See the relation \
between the slider values and the \[CapitalPsi](t) equation. Once you feel \
you understand this effect, Take a Test Using the New Test Button!"]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Input Amplitude", 
          GrayLevel[0]], 
         Slider[
          Dynamic[$CellContext`a], {0, 5}, Appearance -> "Labeled"]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {
      Hold[
       Row[{
         Style["Input Position   ", 
          GrayLevel[0]], 
         Slider[
          Dynamic[$CellContext`p], {0, 5}, Appearance -> "Labeled"]}]], 
      Manipulate`Dump`ThisIsNotAControl}, {{
       Hold[$CellContext`fins$$], UnitBox, "Input signal   "}, {
      UnitBox -> "Pulse", UnitStep -> "Step", SawtoothWave -> "Saw", Sin -> 
       "Sine", $CellContext`F1 -> "F1", $CellContext`Ex -> "Exp"}}, {{
       Hold[$CellContext`system$$], 1, "Systems   "}, {
      1 -> "1", 2 -> "2", 3 -> "3", 4 -> "4", 5 -> "5", 6 -> "6", 7 -> "7", 8 -> 
       "8"}}, {{
       Hold[$CellContext`showInput$$], False, "Show Input Eq"}, {
      True, False}}, {{
       Hold[$CellContext`showOutput$$], False, "           Show Answer Eq"}, {
      True, False}}, {{
       Hold[$CellContext`showPlot$$], True, "         Show Answer Plot"}, {
      True, False}}, {
      Hold[
       Row[{
         Manipulate`Place[1], 
         Manipulate`Place[2], 
         Manipulate`Place[3]}]], Manipulate`Dump`ThisIsNotAControl}}, 
    Typeset`size$$ = {652., {109., 113.}}, Typeset`update$$ = 0, 
    Typeset`initDone$$, Typeset`skipInitDone$$ = 
    False, $CellContext`fins$25422$$ = False, $CellContext`system$25423$$ = 
    False, $CellContext`showInput$25424$$ = 
    False, $CellContext`showOutput$25425$$ = 
    False, $CellContext`showPlot$25426$$ = False}, 
    DynamicBox[Manipulate`ManipulateBoxes[
     2, StandardForm, 
      "Variables" :> {$CellContext`fins$$ = UnitBox, $CellContext`showInput$$ = 
        False, $CellContext`showOutput$$ = False, $CellContext`showPlot$$ = 
        True, $CellContext`system$$ = 1}, "ControllerVariables" :> {
        Hold[$CellContext`fins$$, $CellContext`fins$25422$$, False], 
        Hold[$CellContext`system$$, $CellContext`system$25423$$, False], 
        Hold[$CellContext`showInput$$, $CellContext`showInput$25424$$, False], 
        Hold[$CellContext`showOutput$$, $CellContext`showOutput$25425$$, 
         False], 
        Hold[$CellContext`showPlot$$, $CellContext`showPlot$25426$$, False]}, 
      "OtherVariables" :> {
       Typeset`show$$, Typeset`bookmarkList$$, Typeset`bookmarkMode$$, 
        Typeset`animator$$, Typeset`animvar$$, Typeset`name$$, 
        Typeset`specs$$, Typeset`size$$, Typeset`update$$, Typeset`initDone$$,
         Typeset`skipInitDone$$}, "Body" :> GraphicsRow[{
         Plot[
          $CellContext`sys[$CellContext`a, $CellContext`t, $CellContext`p], \
{$CellContext`t, 0, 10}, ImageSize -> $CellContext`S, PlotStyle -> Thick, 
          PlotLabel -> Dynamic[
            If[$CellContext`showInput$$, 
             $CellContext`sys[$CellContext`a, $CellContext`t, $CellContext`p],
              "\[CapitalPsi][t]"]], 
          PlotRange -> {{0, 10}, {1.2 (-6), 1.2 6}}], 
         Dynamic[$CellContext`sysequation = {
             $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p], $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p]^2, 
             $CellContext`sys[
             2 $CellContext`a, $CellContext`g, $CellContext`p], 
             $CellContext`sys[$CellContext`a, $CellContext`g^2, \
$CellContext`p], 
             $CellContext`sys[$CellContext`a, 
              2 $CellContext`g, $CellContext`p], 
             $CellContext`sys[$CellContext`g $CellContext`a, $CellContext`g, \
$CellContext`p], 
             Piecewise[{{
                $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p], $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p] < 3}, {
               3, $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p] > 
                3}}], $CellContext`sys[$CellContext`a, $CellContext`g, \
$CellContext`p] + 1}; If[$CellContext`showPlot$$, 
            Plot[
             
             Part[$CellContext`sysequation, $CellContext`system$$], \
{$CellContext`g, 0, 10}, ImageSize -> $CellContext`S, PlotStyle -> Thick, 
             PlotLabel -> Dynamic[
               If[$CellContext`showOutput$$, 
                Part[$CellContext`sysequation, $CellContext`system$$], 
                "\[CapitalPhi][t]"]], 
             PlotRange -> {{0, 10}, {(-1.2) 6, 1.2 6}}], 
            Dynamic[
             If[$CellContext`showOutput$$, 
              Part[$CellContext`sysequation, $CellContext`system$$], 
              "\[CapitalPhi][t]"]]]]}], "Specifications" :> {
        Tooltip[
         Style["\nStart here!", 
          RGBColor[1, 0, 0], 8], 
         "Slide A (amplitude) and \!\(\*SubscriptBox[\(t\), \(0\)]\) and \
observe the effect. What do you think each slider does? See the relation \
between the slider values and the \[CapitalPsi](t) equation. Once you feel \
you understand this effect, Take a Test Using the New Test Button!"], 
        Row[{
          Style["Input Amplitude", 
           GrayLevel[0]], 
          Slider[
           Dynamic[$CellContext`a], {0, 5}, Appearance -> "Labeled"]}], 
        Row[{
          Style["Input Position   ", 
           GrayLevel[0]], 
          Slider[
           Dynamic[$CellContext`p], {0, 5}, Appearance -> 
           "Labeled"]}], {{$CellContext`fins$$, UnitBox, "Input signal   "}, {
         UnitBox -> "Pulse", UnitStep -> "Step", SawtoothWave -> "Saw", Sin -> 
          "Sine", $CellContext`F1 -> "F1", $CellContext`Ex -> 
          "Exp"}}, {{$CellContext`system$$, 1, "Systems   "}, {
         1 -> "1", 2 -> "2", 3 -> "3", 4 -> "4", 5 -> "5", 6 -> "6", 7 -> "7",
           8 -> "8"}}, {{$CellContext`showInput$$, False, "Show Input Eq"}, {
         True, False}, ControlPlacement -> 
         1}, {{$CellContext`showOutput$$, False, 
          "           Show Answer Eq"}, {True, False}, ControlPlacement -> 
         2}, {{$CellContext`showPlot$$, True, "         Show Answer Plot"}, {
         True, False}, ControlPlacement -> 3}, 
        Row[{
          Manipulate`Place[1], 
          Manipulate`Place[2], 
          Manipulate`Place[3]}]}, "Options" :> {FrameLabel -> {"", "", 
          Style["LTI Drill", Large], ""}, LocalizeVariables -> True, Deployed -> 
        True}, "DefaultOptions" :> {}],
     ImageSizeCache->{711., {236., 241.}},
     SingleEvaluation->True],
    Deinitialization:>None,
    DynamicModuleValues:>{},
    Initialization:>({$CellContext`a = 1; $CellContext`p = 
        0; $CellContext`S = {300, 200}; $CellContext`sys[
          Pattern[$CellContext`az$, 
           Blank[]], 
          Pattern[$CellContext`tz$, 
           Blank[]], 
          Pattern[$CellContext`pz$, 
           
           Blank[]]] := $CellContext`az$ $CellContext`fins$$[$CellContext`tz$ - \
$CellContext`pz$]; $CellContext`F1[
          Pattern[$CellContext`tz, 
           Blank[]]] := 
        Piecewise[{{$CellContext`tz^2, 0 < $CellContext`tz < 2}, {
           0, $CellContext`tz <= 0}, {-$CellContext`tz + 4, 
            Inequality[2, LessEqual, $CellContext`tz, Less, 3]}, {
           0, $CellContext`tz >= 3}}]; $CellContext`Ex[
          Pattern[$CellContext`tz, 
           Blank[]]] := Exp[0.5 $CellContext`tz]; Null}; 
     Typeset`initDone$$ = True),
    SynchronousInitialization->True,
    UnsavedVariables:>{Typeset`initDone$$},
    UntrackedVariables:>{Typeset`size$$}], "Manipulate",
   Deployed->True,
   StripOnInput->False],
  Manipulate`InterpretManipulate[1]]], "Output",
 CellChangeTimes->{
  3.521225044036068*^9, 3.521225109818837*^9, 3.5214961913283243`*^9, 
   3.5214962585870447`*^9, {3.5220065617687263`*^9, 3.522006571940559*^9}, 
   3.522173542053318*^9, {3.522439391978936*^9, 3.522439417490541*^9}, 
   3.5224395518388767`*^9, 3.522781286979706*^9}]
}, {2}]]
},
WindowSize->{1036, 685},
WindowMargins->{{Automatic, 94}, {-26, Automatic}},
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
Cell[CellGroupData[{
Cell[572, 22, 20568, 442, 640, "Input"],
Cell[21143, 466, 8657, 179, 494, "Output"]
}, {2}]]
}
]
*)

(* End of internal cache information *)

(* NotebookSignature vwDNfqEBA@i7NCwaDT6@RrtL *)
