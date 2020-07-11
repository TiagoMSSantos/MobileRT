#!/usr/bin/gnuplot --persist

###############################################################################
# Enable print
###############################################################################
set print "-"
print "GNU Plot Script"
###############################################################################
###############################################################################


###############################################################################
# Define terminal
###############################################################################
reset
set datafile separator ","
#set output 'graph.png'
set terminal wxt size 1700, 800 enhanced font "Verdana,8" title "Performance comparation" persist raise ctrl
set key outside
###############################################################################
###############################################################################


###############################################################################
# Define axis - remove border on top and right and set color to black
###############################################################################
set linestyle 1 linecolor rgb 'black' linetype 1
set border 3 back linestyle 1
set tics nomirror
###############################################################################
###############################################################################


###############################################################################
# Array to store file names and colors
###############################################################################
arrayGet(name, i) = value(sprintf("_%s_%i", name, i))
arraySet(name, i, value) = sprintf("_%s_%i=\"%s\"", name, i, value)
arrayPush(name, value) = arraySet(name, index = index + 1, value)
###############################################################################
###############################################################################


###############################################################################
# Colors of graphs
###############################################################################
index = 0
eval arrayPush("COLORS", "red")
eval arrayPush("COLORS", "green")
eval arrayPush("COLORS", "blue")
eval arrayPush("COLORS", "purple")
eval arrayPush("COLORS", "orange")
eval arrayPush("COLORS", "brown")
eval arrayPush("COLORS", "steelblue")
eval arrayPush("COLORS", "orchid")
eval arrayPush("COLORS", "turquoise")
eval arrayPush("COLORS", "grey")
eval arrayPush("COLORS", "gold")
eval arrayPush("COLORS", "navy")
eval arrayPush("COLORS", "skyblue")
eval arrayPush("COLORS", "magenta")
eval arrayPush("COLORS", "salmon")
eval arrayPush("COLORS", "olive")
eval arrayPush("COLORS", "violet")
eval arrayPush("COLORS", "plum")
eval arrayPush("COLORS", "sandybrown")
eval arrayPush("COLORS", "pink")
eval arrayPush("COLORS", "bisque")
eval arrayPush("COLORS", "slategrey")
eval arrayPush("COLORS", "chartreuse")

eval arrayPush("COLORS", "black")
eval arrayPush("COLORS", "dark-magenta")
eval arrayPush("COLORS", "dark-cyan")
eval arrayPush("COLORS", "dark-orange")
eval arrayPush("COLORS", "dark-spring-green")
eval arrayPush("COLORS", "dark-red")
eval arrayPush("COLORS", "dark-chartreuse")
eval arrayPush("COLORS", "dark-green")
eval arrayPush("COLORS", "dark-khaki")
eval arrayPush("COLORS", "dark-goldenrod")
eval arrayPush("COLORS", "dark-violet")
eval arrayPush("COLORS", "dark-plum")
eval arrayPush("COLORS", "dark-olivegreen")
###############################################################################
###############################################################################


###############################################################################
# Files to plot
###############################################################################
index = 0
startSep=0
endSep = startSep + strstrt(filenames[startSep:], separator)
filePath = filenames[startSep : endSep - 1]
if (filePath eq "") {
  print 'filePath: "' . filePath . '" invalid'
  exit gnuplot
}
eval arrayPush("FILES", filePath)
endSep = endSep + 1

do for [i=2:files] {
  startSep = endSep
  endSep = startSep + strstrt(filenames[startSep:], separator)
  filePath = filenames[startSep : endSep - 2]
  eval arrayPush("FILES", filePath)
}
###############################################################################
###############################################################################


###############################################################################
# Stats to get min and max values
###############################################################################
X_min = 0
X_max = 0
Y_min = 0
Y_max = 0

do for [i=1:files] {
  filePath = arrayGet("FILES", i)
  print "filePath: '".filePath."'"
  fileParsed = "< awk -v speedup=".speedup." -f ./scripts/plot/parser_median.awk ".filePath
  print "fileParsed: '".fileParsed."'"
  stats fileParsed using 1 nooutput name 'Fx_'
  stats fileParsed using 2 nooutput name 'Fy_'

  X_min = Fx_min < X_min? Fx_min : X_min
  X_max = Fx_max > X_max? Fx_max : X_max
  Y_min = Fy_min < Y_min? Fy_min : Y_min
  Y_max = Fy_max > Y_max? Fy_max : Y_max
}

Y_tick = (Y_max - Y_min) / 30
###############################################################################
###############################################################################


###############################################################################
# Axis labels
###############################################################################
set xlabel '#Threads'
set xrange [0 : 0<*]
set xtics X_min, 1, X_max offset graph 0, graph 0

if (speedup eq "1") {
  speedup = 1
  labelY = 'Speed up'
} else {
  speedup = 0
  labelY = 'Time (s)'
}

set ylabel labelY
set yrange [0 : 0<*]
set ytics Y_min, Y_tick, Y_max offset graph 0, graph 0
###############################################################################
###############################################################################


###############################################################################
# Line definitions
###############################################################################
set linestyle 1 pointtype 7 pointsize 1.0 linetype 3 linewidth 2.5 dashtype 3
###############################################################################
###############################################################################


###############################################################################
# Plot
###############################################################################
plot \
for [i = 1 : files] \
  filePath = arrayGet("FILES", i) \
  name = filePath[0 : strstrt(filePath[0:], ".dat") - 1] \
  file = "< awk -v speedup=" . speedup . " -f ./scripts/plot/parser_median.awk " . filePath \
  file using 1:2 title name \
  with linespoints linestyle 1 linecolor rgb arrayGet("COLORS", i)
###############################################################################
###############################################################################
