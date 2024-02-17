

function toggleComment(element) {
    var commentContent = element.querySelector('.comment-content');
    var toggleButton = element.querySelector('.toggle-comment');

    if (commentContent) {
        commentContent.classList.toggle('expanded');
        toggleButton.textContent = commentContent.classList.contains('expanded') ? 'Свернуть комментарий' : 'Развернуть комментарий';
    }
}
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.form-thread-btn').forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            var fileUrl = this.getAttribute('data-fileurl');

            var csrfTokenInput = document.querySelector('input[name="_csrf"]');
            if (!csrfTokenInput) {
                console.error('CSRF token not found');
                return; // Прекращаем выполнение функции, если CSRF токен не найден
            }
            var csrfToken = csrfTokenInput.value;

            fetch('/pictures/start-thread-processing', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ fileUrl: fileUrl })
            }).then(response => {
                if (response.ok) {
                    // Проверяем, что кнопка существует, прежде чем изменять её стиль
                    if (this) {
                        this.style.display = 'none';
                    }
                } else {
                    console.error('Failed to start thread processing');
                }
            }).catch(error => {
                console.error('Error:', error);
            });
        });
    });
});


document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.comment-content').forEach(function (comment) {
        if (comment.textContent.length > 150) {
            comment.classList.add('hidden');
            comment.nextElementSibling.style.display = 'block'; // Показать кнопку
        }
    });
});

function toggleSize(selectedElement) {
    // Получаем все медиа-элементы
    var allMediaElements = document.querySelectorAll('.media');

    // Проходим по всем медиа-элементам
    allMediaElements.forEach(function (element) {
        // Если это не выбранный элемент, удаляем класс 'enlarged'
        if (element !== selectedElement) {
            element.classList.remove('enlarged');
        }
    });

    // Переключаем класс 'enlarged' для выбранного элемента
    selectedElement.classList.toggle('enlarged');
}

function updateItemCount() {
    var itemCount = document.getElementById('itemCount').value;
    var currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('size', itemCount);
    window.location.href = currentUrl.href;
}

document.addEventListener('DOMContentLoaded', function () {
    var currentUrl = new URL(window.location.href);
    var size = currentUrl.searchParams.get('size') || '10'; // Установка значения по умолчанию
    document.getElementById('itemCount').value = size;
});


var socket = new SockJS('/websocket');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    stompClient.subscribe('/topic/threadStatus', function (notification) {
        // Обработка полученного уведомления
        alert(notification.body);
        // Здесь можно добавить логику для изменения состояния кнопки
    });
});