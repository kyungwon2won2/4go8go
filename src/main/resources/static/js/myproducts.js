document.addEventListener('DOMContentLoaded', function() {
    // 탭 전환 처리
    document.querySelectorAll('.nav-tabs .nav-link').forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();

            // 현재 활성 탭 비활성화
            document.querySelector('.nav-tabs .nav-link.active').classList.remove('active');

            // 클릭한 탭 활성화
            this.classList.add('active');

            const status = this.getAttribute('data-status');

            // 상태별 상품 필터링 (AJAX)
            filterProductsByStatus(status);
        });
    });

    // 정렬 변경 이벤트 (AJAX로 변경)
    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            const sortBy = this.value;
            loadProductsWithCurrentFilters({ sort: sortBy });
        });
    }

    // 검색 버튼 이벤트
    const searchButton = document.querySelector('.search-button');
    const searchInput = document.querySelector('.search-input');

    if (searchButton && searchInput) {
        // 검색 입력 필드에서 엔터키 처리
        searchInput.addEventListener('keyup', function(event) {
            if (event.key === 'Enter') {
                searchProducts();
            }
        });

        // 검색 버튼 클릭 처리
        searchButton.addEventListener('click', searchProducts);
    }

    // URL 파라미터에 따른 초기 필터 상태 설정
    initializeFiltersFromUrl();

    // 상품 데이터 속성 설정
    setProductDataAttributes();
    
    // 초기 로드된 카드들에도 클릭 이벤트 추가
    addClickEventsToExistingCards();
});

/**
 * 상품 카드에 데이터 속성 추가
 */
function setProductDataAttributes() {
    const productCards = document.querySelectorAll('#products-container .col');

    productCards.forEach(card => {
        // 상품 ID를 데이터 속성으로 저장
        const productIdMatch = card.querySelector('.action-button')?.getAttribute('onclick')?.match(/\d+/);
        if (productIdMatch && productIdMatch[0]) {
            card.setAttribute('data-product-id', productIdMatch[0]);
        }
    });
}

/**
 * URL 파라미터를 기반으로 필터 상태 초기화
 */
function initializeFiltersFromUrl() {
    const params = new URLSearchParams(window.location.search);

    // 정렬 상태 설정
    const sortValue = params.get('sort');
    if (sortValue) {
        const sortSelect = document.getElementById('sort-select');
        if (sortSelect) {
            sortSelect.value = sortValue;
        }
    }

    // 검색어 설정
    const searchValue = params.get('search');
    if (searchValue) {
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.value = searchValue;
        }
    }

    // 상태 탭 설정 (필터링 호출 없이 UI만 설정)
    const statusValue = params.get('status');
    if (statusValue) {
        const statusTab = document.querySelector(`.nav-link[data-status="${statusValue}"]`);
        if (statusTab) {
            document.querySelector('.nav-tabs .nav-link.active').classList.remove('active');
            statusTab.classList.add('active');
        }
    }
}

/**
 * 상태별 상품 필터링 (AJAX)
 */
function filterProductsByStatus(status) {
    const params = { status: status === 'all' ? null : status };
    loadProductsWithCurrentFilters(params);
}

/**
 * 현재 필터 상태를 유지하면서 상품 로드 (AJAX)
 */
function loadProductsWithCurrentFilters(newParams = {}) {
    // 현재 URL 파라미터 가져오기
    const currentParams = new URLSearchParams(window.location.search);

    // 새로운 파라미터 적용
    Object.keys(newParams).forEach(key => {
        if (newParams[key] === null || newParams[key] === undefined) {
            currentParams.delete(key);
        } else {
            currentParams.set(key, newParams[key]);
        }
    });

    // 페이지는 1로 초기화 (정렬, 검색, 필터 변경 시)
    if (!newParams.hasOwnProperty('page')) {
        currentParams.delete('page');
    }

    // AJAX 요청
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/user/product/my/api?${currentParams.toString()}`, {
        method: 'GET',
        headers: {
            [header]: token
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('데이터 로드 실패');
            }
            return response.json();
        })
        .then(data => {
            // 상품 목록 업데이트
            updateProductList(data.products, data.selectedStatus);

            // 페이징 정보 업데이트
            updatePagination(data);

            // URL 업데이트 (페이지 새로고침 없이)
            const newUrl = window.location.pathname + '?' + currentParams.toString();
            window.history.replaceState({}, '', newUrl);

            // 빈 상태 메시지 처리
            toggleEmptyState(data.products.length === 0);

            // 상품 데이터 속성 재설정
            setProductDataAttributes();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('데이터를 불러오는 중 오류가 발생했습니다.');
        });
}

/**
 * 상품 검색 처리 (AJAX로 변경)
 */
function searchProducts() {
    const searchInput = document.querySelector('.search-input');
    const searchQuery = searchInput ? searchInput.value.trim() : '';

    if (searchQuery) {
        loadProductsWithCurrentFilters({ search: searchQuery });
    }
}

/**
 * 상품 목록 DOM 업데이트
 */
function updateProductList(products, selectedStatus) {
    const container = document.getElementById('products-container');
    container.innerHTML = '';

    products.forEach(product => {
        const productCard = createProductCard(product, selectedStatus);
        container.appendChild(productCard);
    });
}

/**
 * 초기 로드된 카드들에 클릭 이벤트 추가
 */
function addClickEventsToExistingCards() {
    document.querySelectorAll('#products-container .col').forEach(card => {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            // 버튼 클릭 시에는 이동하지 않음
            if (e.target.closest('.action-button')) {
                return;
            }
            
            // 상품 ID 찾기
            const productId = findProductIdFromCard(this);
            if (productId) {
                window.location.href = `/product/${productId}`;
            }
        });
    });
}

/**
 * 카드에서 상품 ID 찾기
 */
function findProductIdFromCard(cardElement) {
    // 버튼의 onclick 속성에서 상품 ID 추출
    const buttons = cardElement.querySelectorAll('.action-button');
    for (let button of buttons) {
        const onclick = button.getAttribute('onclick');
        if (onclick) {
            const match = onclick.match(/\d+/);
            if (match) {
                return match[0];
            }
        }
    }
    return null;
}

/**
 * 카드 클릭 이벤트 (전역 함수)
 */
function goToProductDetail(postId, event) {
    // 버튼 클릭이 아닌 경우에만 이동
    if (!event.target.closest('.action-button')) {
        window.location.href = `/product/${postId}`;
    }
}

/**
 * 상품 카드 HTML 생성
 */
function createProductCard(product, selectedStatus) {
    const col = document.createElement('div');
    col.className = 'col';
    col.style.cursor = 'pointer'; // 클릭 가능함을 표시
    col.setAttribute('data-post-id', product.postId); // 상품 ID 저장

    // 상품 상태 배지 HTML
    let statusBadgeHtml = '';
    if (product.tradeStatus === 'AVAILABLE') {
        statusBadgeHtml = '<div class="status-badge status-available">판매중</div>';
    } else if (product.tradeStatus === 'COMPLETED') {
        statusBadgeHtml = '<div class="status-badge status-completed">판매완료</div>';
    }

    // 다중 이미지 배지 HTML
    const multiImageBadge = product.hasMultipleImages ?
        '<div class="multi-image-badge"><i class="bi bi-images"></i></div>' : '';

    // 액션 버튼 HTML (상태에 따라 다름)
    let actionButtonsHtml = '';

    if (selectedStatus !== 'purchased') {
        // 내가 등록한 상품 버튼들 (원본과 동일한 구조로 div 래퍼 추가)
        actionButtonsHtml = `
            <div>
                <button class="action-button edit-button" onclick="event.stopPropagation(); location.href='/user/product/${product.postId}/edit'">
                    수정
                </button>
                <button class="action-button delete-button" onclick="event.stopPropagation(); confirmDelete(${product.postId})">
                    삭제
                </button>`;
        if (product.tradeStatus === 'AVAILABLE') {
            actionButtonsHtml += `
                <button class="action-button complete-button" onclick="event.stopPropagation(); openBuyerSelectModal(${product.postId})">
                    <i class="bi bi-check-circle"></i> 판매하기
                </button>`;
        }
        actionButtonsHtml += `</div>`;
    } else {
        // 구매한 상품 버튼들 (원본과 동일한 구조로 div 래퍼 추가)
        actionButtonsHtml = `
            <div>
                <button class="action-button review-button" onclick="event.stopPropagation(); writeReview(${product.postId})">
                    <i class="bi bi-star"></i> 리뷰작성
                </button>
            </div>
        `;
    }

    // 상품 카드 HTML 구성
    col.innerHTML = `
        <div class="card h-100" onclick="goToProductDetail(${product.postId}, event)">
            <div class="product-image-container">
                <img src="${product.imageUrl}" alt="상품 이미지" class="product-image">
                ${statusBadgeHtml}
                ${multiImageBadge}
            </div>

            <div class="card-body">
                <h5 class="card-title">${product.title}</h5>
                <p class="product-price">${formatPrice(product.price)}원</p>
                
                <!-- 상품 통계 -->
                <div class="product-stats">
                    <div class="product-stat-item">
                        <i class="bi bi-eye"></i>
                        <span>${product.viewCount || 0}</span>
                    </div>
                    <div class="product-stat-item">
                        <i class="bi bi-heart"></i>
                        <span>${product.likeCount || 0}</span>
                    </div>
                    <div class="product-stat-item">
                        <i class="bi bi-chat"></i>
                        <span>${product.chatCount || 0}</span>
                    </div>
                </div>
                
                <!-- 작업 버튼 -->
                <div class="action-buttons">
                    ${actionButtonsHtml}
                </div>
            </div>
            
            <div class="card-footer bg-white border-0 text-end">
                <small class="text-muted">${formatDate(product.createdAt)}</small>
            </div>
        </div>
    `;

    return col;
}

/**
 * 페이징 정보 업데이트
 */
function updatePagination(data) {
    const paginationNav = document.querySelector('nav[aria-label="Page navigation"]');
    if (!paginationNav) return;

    const paginationUl = paginationNav.querySelector('.pagination');
    if (!paginationUl) return;

    // 페이징이 필요없는 경우 숨김
    if (data.totalPages <= 1) {
        paginationNav.style.display = 'none';
        return;
    }

    paginationNav.style.display = 'block';
    paginationUl.innerHTML = '';

    // 이전 페이지 버튼
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${data.currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `
        <a class="page-link" href="#" data-page="${data.currentPage - 1}" aria-label="Previous">
            <span aria-hidden="true">&laquo;</span>
        </a>
    `;
    paginationUl.appendChild(prevLi);

    // 페이지 번호들
    for (let i = 1; i <= data.totalPages; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item ${i === data.currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
        paginationUl.appendChild(pageLi);
    }

    // 다음 페이지 버튼
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${data.currentPage === data.totalPages ? 'disabled' : ''}`;
    nextLi.innerHTML = `
        <a class="page-link" href="#" data-page="${data.currentPage + 1}" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
        </a>
    `;
    paginationUl.appendChild(nextLi);

    // 페이지 링크 클릭 이벤트
    paginationUl.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = parseInt(this.getAttribute('data-page'));
            if (page && page !== data.currentPage) {
                loadProductsWithCurrentFilters({ page: page });
            }
        });
    });
}

/**
 * 빈 상태 메시지 토글
 */
function toggleEmptyState(isEmpty) {
    const emptyState = document.querySelector('.empty-state');
    if (emptyState) {
        emptyState.style.display = isEmpty ? 'flex' : 'none';
    }
}

/**
 * 가격 포맷팅
 */
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}.${month}.${day} ${hours}:${minutes}`;
}

// ==================== 기존 함수들 (수정 없음) ====================

//구매자 선택 모달
function openBuyerSelectModal(postId) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/user/product/${postId}/chat-participants`, {
        method: 'GET',
        headers: {
            [header]: token
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('채팅 참여자 조회 실패');
            }
            return response.json();
        })
        .then(participants => {
            const buyerListContainer = document.getElementById('buyer-list');
            buyerListContainer.innerHTML = '';

            if (participants.length === 0) {
                buyerListContainer.innerHTML = '<p class="text-muted">아직 관심을 보인 구매자가 없습니다.</p>';
                const completeButton = document.getElementById('completeButton');
                if (completeButton) completeButton.style.display = 'none';
            } else {
                displayParticipants(participants);
                const completeButton = document.getElementById('completeButton');
                if (completeButton) completeButton.style.display = 'inline-block';
            }

            document.getElementById('selectedPostId').value = postId;
            const buyerSelectModal = new bootstrap.Modal(document.getElementById('buyerSelectModal'));
            buyerSelectModal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('채팅 참여자 조회 중 오류가 발생했습니다.');
        });
}

//모달에 참여자 목록 출력
function displayParticipants(participants) {
    const buyerList = document.getElementById('buyer-list');
    buyerList.innerHTML = '';

    participants.forEach(participant => {
        const buyerItem = document.createElement('div');
        buyerItem.className = 'form-check mb-2';
        buyerItem.innerHTML = `
            <input class="form-check-input" type="radio" name="buyerRadio" id="buyer_${participant.userId}" value="${participant.userId}">
            <label class="form-check-label" for="buyer_${participant.userId}">
                <div class="d-flex align-items-center">
                    <img src="${participant.profileImage || '/image/profile.png'}" alt="프로필" class="rounded-circle me-2" width="32" height="32">
                    <div>
                        <div class="fw-bold">${participant.nickname}</div>
                        <small class="text-muted">${participant.email}</small>
                    </div>
                </div>
            </label>
        `;
        buyerList.appendChild(buyerItem);
    });
}

//선택된 구매자 ID 가져오기
function getSelectedBuyerId() {
    const selectedRadio = document.querySelector('input[name="buyerRadio"]:checked');
    return selectedRadio ? selectedRadio.value : null;
}

//판매 완료 처리 in 모달
function completeTransaction() {
    const postId = document.getElementById('selectedPostId').value;
    const buyerId = getSelectedBuyerId();

    if (!buyerId) {
        alert('구매자를 선택해주세요.');
        return;
    }

    completeTransactionDirect(postId, buyerId);
}

//판매 완료 api호출
function completeTransactionDirect(postId, buyerId) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // FormData 생성
    const formData = new FormData();
    formData.append('buyerId', buyerId);

    fetch(`/user/product/${postId}/complete`, {
        method: 'POST',
        headers: {
            [header]: token
        },
        body: formData
    })
        .then(response => {
            if (response.ok) {
                // 모달이 열려있으면 닫기
                const modal = bootstrap.Modal.getInstance(document.getElementById('buyerSelectModal'));
                if (modal) {
                    modal.hide();
                }

                alert('판매가 완료되었습니다.');
                // 페이지 새로고침 대신 현재 데이터 다시 로드
                loadProductsWithCurrentFilters();
            } else {
                return response.json().then(data => {
                    throw new Error(data.error || '판매완료 처리에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert(error.message || '판매완료 처리 중 오류가 발생했습니다.');
        });
}

// 삭제 확인 함수 (기존 HTML에서 호출)
function confirmDelete(postId) {
    document.getElementById('deleteProductId').value = postId;
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    deleteModal.show();
}

// 상품 삭제 처리
function deleteProduct() {
    const productId = document.getElementById('deleteProductId').value;

    // CSRF 토큰
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // AJAX 요청으로 삭제
    fetch(`/user/product/${productId}/my`, {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
        .then(response => {
            if (response.ok) {
                // 모달 닫기
                bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();

                // 페이지 새로고침 대신 현재 데이터 다시 로드
                loadProductsWithCurrentFilters();
            } else {
                throw new Error('삭제에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('상품 삭제 중 오류가 발생했습니다.');
        });
}

// 리뷰 작성 모달 열기
function openReviewModal(postId) {
    // 모달 초기화
    resetReviewModal();

    // 상품 ID 저장
    document.getElementById('reviewPostId').value = postId;

    // 별점 초기화 (5점으로 설정)
    initializeStarRating();

    // 모달 열기
    const reviewModal = new bootstrap.Modal(document.getElementById('reviewModal'));
    reviewModal.show();
}

// 별점 초기화 + 별점 클릭 이벤트
function initializeStarRating() {
    const starRatingContainer = document.getElementById('star-rating');

    // 5개 별 생성
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
        starsHtml += `<span class="star" data-rating="${i}">★</span>`;
    }
    starRatingContainer.innerHTML = starsHtml;

    // 기본 5점으로 설정
    setStarRating(5);

    // 별 클릭 이벤트 추가
    starRatingContainer.querySelectorAll('.star').forEach(star => {
        star.addEventListener('click', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            setStarRating(rating);
        });
    });
}

// 별점 저장
function setStarRating(rating) {
    // 선택된 평점 저장
    document.getElementById('selectedRating').value = rating;

    // 별 활성화 상태 업데이트
    const stars = document.querySelectorAll('#star-rating .star');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

// 리뷰 모달 초기화
function resetReviewModal() {
    // 텍스트 영역 초기화
    document.getElementById('reviewContent').value = '';

    // 별점 5점으로 초기화
    document.getElementById('selectedRating').value = '5';

    // 상품 ID 초기화
    document.getElementById('reviewPostId').value = '';
}

// 리뷰 제출
function submitReview() {
    const postId = document.getElementById('reviewPostId').value;
    const rating = document.getElementById('selectedRating').value;
    const content = document.getElementById('reviewContent').value.trim();

    // 입력 검증
    if (!content) {
        alert('리뷰 내용을 입력해주세요.');
        return;
    }

    if (content.length < 10) {
        alert('리뷰 내용을 10자 이상 입력해주세요.');
        return;
    }

    // CSRF 토큰
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 리뷰 데이터
    const reviewData = {
        postId: parseInt(postId),
        point: parseInt(rating),
        content: content
    };

    // AJAX 요청
    fetch('/user/review/create/api', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify(reviewData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 성공 시
                alert('리뷰가 성공적으로 작성되었습니다.');

                // 모달 닫기
                const modal = bootstrap.Modal.getInstance(document.getElementById('reviewModal'));
                if (modal) {
                    modal.hide();
                }

                // 리뷰 버튼 비활성화
                disableReviewButton(postId);

                // 필요시 페이지 새로고침 또는 데이터 다시 로드
                // loadProductsWithCurrentFilters(); // AJAX 방식인 경우
                // location.reload(); // 간단한 새로고침

            } else {
                // 실패 시
                alert(data.message || '리뷰 작성에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('리뷰 작성 중 오류가 발생했습니다.');
        });
}

// 리뷰 작성 함수 (구매한 상품용)
function writeReview(postId) {
    openReviewModal(postId);
}

// 리뷰 작성시 비활성화해주는 함수
function disableReviewButton(postId) {
    // onclick 속성에서 postId를 찾음
    const reviewButtons = document.querySelectorAll('.review-button');
    reviewButtons.forEach(button => {
        const onclick = button.getAttribute('onclick');
        if (onclick && onclick.includes(postId)) {
            button.disabled = true;
            button.innerHTML = '<i class="bi bi-check-circle"></i> 리뷰 완료';
            button.classList.add('disabled');
        }
    });
}