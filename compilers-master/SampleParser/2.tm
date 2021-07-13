* C-Minus Compilation to TM Code
* File: 2.tm
* Standard prelude
  0:    LD  6,0(0)  load gp with maxaddress
  1:   LDA  5,0(6)  copy gp to fp
  2:    ST  0,0(0)  clear location 0
* Jump around i/o routines here
* Code for input routine
  4:    ST  0,-1(5)  store return
  5:    IN  0,0,0  input
  6:    LD  7,-1(5)  return to caller
* Code for output routine
  7:    ST  0,-1(5)  store return
  8:    LD  0,-2(5)  load output value
  9:   OUT  0,0,0  output
 10:    LD  7,-1(5)  return to caller
  3:   LDA  7,7(7)  jump around i/o code
* End of standard prelude
* Processing function: main
 12:    ST  0,-1(5)  save return address for main
* processing local var: j
* -> compound
* processing local var: x
* -> id
* looking up id: x
 13:   LDA  0,-3(5) 	load id x address
* <- id
 14:    ST  0,-4(5) 	op: push left
 15:   LDC  0,5(0) 	load constant 5
 16:    ST  0,-5(5) 	store constant 5
 17:    LD  0,-4(5) 	load variable address
 18:    LD  1,-5(5) 	load register with stored value
 19:    ST  1,0(0) 	store value
* -> id
* looking up id: j
 20:   LDA  0,-2(5) 	load id j address
* <- id
 21:    ST  0,-7(5) 	op: push left
 22:    LD  0,-3(5) 	load reg 0 with x
 23:    ST  0,-9(5) 	store value of x
* -> constant
 24:   LDC  0,1(0) 	load constant int 1
* <- constant
 25:    ST  0,-10(5) 	store the constant
 26:    LD  0,-9(5) 	load value 1 for computation
 27:    LD  1,-10(5) 	load value 2 for computation
 28:   ADD  0,0,1 	add values
 29:    ST  0,-8(5) 	store result of addition
 30:    LD  0,-7(5) 	load register with address for calc
 31:    LD  1,-8(5) 	load register with stored result
 32:    ST  1,0(0) 	store result
* Call of function: output
* -> id
* looking up id: j
 33:    LD  0,-2(5) 	load id j
* <- id
 34:    ST  0,-11(5) 	store arg value
 35:    ST  5,-9(5) 	push ofp
 36:   LDA  5,-9(5) 	push frame
 37:   LDA  0,1(7) 	load ac with return pointer
 38:   LDA  7,-32(7) 	jump to function location
 39:    LD  5,0(5) 	pop frame
* <- call
* <- compound
 40:    LD  7,-1(5)  return back to the caller
 11:   LDA  7,29(7)  jump around fn body
* End of execution
 41:    ST  5,0(5) 	push ofp
 42:   LDA  5,0(5) 	push frame
 43:   LDA  0,1(7) 	load ac with return pointer
 44:   LDA  7,-33(7) 	jump to main's location
 45:    LD  5,0(5) 	pop frame
 46:  HALT  0,0,0   
