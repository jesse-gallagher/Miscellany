jQuery(function($) {
	$.mask.definitions['~'] = '[+-]';
	$('.input-mask-date').mask('99/99/9999');
	$('.input-mask-phone').mask('(999) 999-9999');
})