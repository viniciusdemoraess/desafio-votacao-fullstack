import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('HTTP Error Details:', {
          url: error.url,
          status: error.status,
          statusText: error.statusText,
          error: error.error,
          message: error.message
        });
        
        // Retorna o erro original para que os componentes possam tratar adequadamente
        return throwError(() => error);
      })
    );
  }
}
