// Simple lightbox for gallery thumbnails
(function(){
  function qs(sel, ctx){ return (ctx || document).querySelector(sel); }
  function qsa(sel, ctx){ return Array.from((ctx || document).querySelectorAll(sel)); }

  document.addEventListener('DOMContentLoaded', function(){
    const lightbox = qs('#lightbox');
    const lbImg = qs('#lightbox-img');
    const btnClose = qs('.lb-close');
    const btnPrev = qs('.lb-prev');
    const btnNext = qs('.lb-next');
    let currentList = [];
    let currentIndex = 0;

    function open(list, index){
      currentList = list;
      currentIndex = index;
      lbImg.src = currentList[currentIndex];
      lightbox.style.display = 'flex';
      document.body.style.overflow = 'hidden';
    }
    function close(){
      lightbox.style.display = 'none';
      lbImg.src = '';
      document.body.style.overflow = '';
    }
    function showIndex(i){
      if(!currentList.length) return;
      currentIndex = (i + currentList.length) % currentList.length;
      lbImg.src = currentList[currentIndex];
    }

    // Attach gallery click handlers
    qsa('.gallery img').forEach(function(img){
      img.addEventListener('click', function(e){
        const gallery = img.closest('.gallery');
        const imgs = qsa('img', gallery).map(i => i.src);
        const idx = imgs.indexOf(img.src);
        open(imgs, idx >= 0 ? idx : 0);
      });
    });

    // Close on overlay click (but not on image)
    lightbox.addEventListener('click', function(e){
      if(e.target === lightbox || e.target === btnClose) close();
    });
    btnClose && btnClose.addEventListener('click', close);
    btnPrev && btnPrev.addEventListener('click', function(e){ e.stopPropagation(); showIndex(currentIndex - 1); });
    btnNext && btnNext.addEventListener('click', function(e){ e.stopPropagation(); showIndex(currentIndex + 1); });

    document.addEventListener('keydown', function(e){
      if(lightbox.style.display !== 'flex') return;
      if(e.key === 'Escape') close();
      if(e.key === 'ArrowLeft') showIndex(currentIndex - 1);
      if(e.key === 'ArrowRight') showIndex(currentIndex + 1);
    });
  });
})();
