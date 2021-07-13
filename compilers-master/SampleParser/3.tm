* C-Minus Compilation to TM Code
* File: 3.tm
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
* Processing function: assignX
 12:    ST  0,-1(5)  save return address for assignX
* -> compound
* -> constant
 13:   LDC  0,10(0) 	load constant
* <- constant
 14:    LD  7,-1(5) 	return to caller
* <- return
* <- compound
 15:    LD  7,-1(5)  return back to the caller
 11:   LDA  7,4(7)  jump around fn body
* Processing function: main
 17:    ST  0,-1(5)  save return address for main
* -> compound
* processing local var: x
* -> id
* looking up id: x
 18:   LDA  0,-2(5) 	load id x address
* <- id
 19:    ST  0,-3(5) 	op: push left
* Call of function: assignX
 20:    ST  5,-5(5) 	push ofp
 21:   LDA  5,-5(5) 	push frame
 22:   LDA  0,1(7) 	load ac with return pointer
 23:   LDA  7,-12(7) 	jump to function location
 24:    LD  5,0(5) 	pop frame
* <- call
 25:    LD  1,-3(5) 	load left
 26:    ST  0,0(1) 	store value
* -> while
* while: jump after body comes back here
 27:    LD  0,-2(5) 	load reg 0 with x
 28:    ST  0,-4(5) 	store value of x
* -> constant
 29:   LDC  0,15(0) 	load constant int 15
* <- constant
 30:    ST  0,-5(5) 	store the constant
 31:    LD  1,-4(5) 	op >: load left
 32:   SUB  0,1,0 	subtract for op >
 33:   JLT  0,2(7) 	branch to true
 34:   LDC  0,0(0) 	false case
 35:   LDA  7,1(7) 	unconditional jump
 36:   LDC  0,1(0) 	true case
* while: jump to end belongs here
* -> compound statement
* -> id
* looking up id: x
 38:   LDA  0,-2(5) 	load id x address
* <- id
 39:    ST  0,-6(5) 	op: push left
 40:    LD  0,-2(5) 	load reg 0 with x
 41:    ST  0,-8(5) 	store value of x
* -> constant
 42:   LDC  0,1(0) 	load constant int 1
* <- constant
 43:    ST  0,-9(5) 	store the constant
 44:    LD  0,-8(5) 	load value 1 for computation
 45:    LD  1,-9(5) 	load value 2 for computation
 46:   ADD  0,0,1 	add values
 47:    ST  0,-7(5) 	store result of addition
 48:    LD  0,-6(5) 	load register with address for calc
 49:    LD  1,-7(5) 	load register with stored result
 50:    ST  1,0(0) 	store result
* <- compound statement
 51:   LDA  7,-25(7) 	jump to while condition
 37:   JEQ  0,14(7) 	while: jump to end
* <- while
* Call of function: output
* -> id
* looking up id: x
 52:    LD  0,-2(5) 	load id x
* <- id
 53:    ST  0,-5(5) 	store arg value
 54:    ST  5,-3(5) 	push ofp
 55:   LDA  5,-3(5) 	push frame
 56:   LDA  0,1(7) 	load ac with return pointer
 57:   LDA  7,-51(7) 	jump to function location
 58:    LD  5,0(5) 	pop frame
* <- call
* <- compound
 59:    LD  7,-1(5)  return back to the caller
 16:   LDA  7,43(7)  jump around fn body
* End of execution
 60:    ST  5,0(5) 	push ofp
 61:   LDA  5,0(5) 	push frame
 62:   LDA  0,1(7) 	load ac with return pointer
 63:   LDA  7,-47(7) 	jump to main's location
 64:    LD  5,0(5) 	pop frame
 65:  HALT  0,0,0   
