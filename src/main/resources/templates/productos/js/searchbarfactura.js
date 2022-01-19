  
            var submitIcon = $('.searchbox-icon');
            var inputBox = $('.searchbox-input');
            var vamonos = $('#vamonos');
            var vamonos2 = $('#vamonos2');
            var searchBox = $('.searchbox');
            var searchBox2 = $('.searchbox2');
            var isOpen = false;
            
            
            vamonos.click(function(){            	
            	var opc = $("#opcSearch").val();            	
            	location.href = $("#urlenviar").val()+opc+"/"+$("#btn-search").val();
            });
            vamonos2.click(function(){            	
            	            	
            	location.href = $("#urlenviar").val()+"ocultos"+"/"+$("#btn-search").val();
            });
                        
            submitIcon.click(function(){
                if(isOpen == false){
                    searchBox.addClass('searchbox-open');
                    inputBox.focus();
                    isOpen = true;
                    
                } else {                	
                    searchBox.removeClass('searchbox-open');
                    inputBox.focusout();
                    isOpen = false;
                    
                }
            });  
             submitIcon.mouseup(function(){
                    return false;
                });
            searchBox.mouseup(function(){
                    return false;
                });
            $(document).mouseup(function(){
                    if(isOpen == true){
                        $('.searchbox-icon').css('display','block');
                        
                        submitIcon.click();
                    }
                });
        
            function buttonUp(){
            	
                var inputVal = $('.searchbox-input').val();
                inputVal = $.trim(inputVal).length;
                if( inputVal !== 0){
                    $('.searchbox-icon').css('display','none');
                } else {
                	
                    $('.searchbox-input').val('');
                    $('.searchbox-icon').css('display','block');
                    var opc = $("#opcSearch").val();                    
                    $('#formsearch').attr('action', $("#urlenviar").val()+"ocultos"+"/"+$("#btn-search").val());
                }
            }
