let imgn = 2;
function addcl() {
	let parent = this.parentNode.parentNode;
	parent.classList.add("focus");
}

function remcl() {
	let parent = this.parentNode.parentNode;
	if (this.value == "") {
		parent.classList.remove("focus");
	}
}

// $('input[type=text]').forEach(input => {
// input.addEventListener("focus", addcl);
// input.addEventListener("blur", remcl);
// });

function cleanh51() {
	document.getElementsByTagName("h5")[1].style.display = 'none';
}
function cleanh5() {
	document.getElementsByTagName("h5")[2].style.display = 'none';
}
function changefunction() {
	//console.log(imgn);
	var x = location.origin;
	document.getElementById("img2").src = x + "/images/phone" + imgn + ".png";
	$("#img2").fadeOut(2000);

	$("#img2").fadeIn(2000);
	imgn = (imgn == 4) ? imgn = 2 : imgn + 1;

}
var usrtxt = document.getElementById('usrtxt');
var usrtxt_va = usrtxt.value;
console.log(usrtxt_va);
if (usrtxt_va != "") {
	cleanh5();
	cleanh51();
}


